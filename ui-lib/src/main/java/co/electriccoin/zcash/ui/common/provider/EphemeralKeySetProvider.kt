package co.electriccoin.zcash.ui.common.provider

import cash.z.ecc.android.sdk.model.AccountUuid
import co.electriccoin.zcash.preference.EncryptedPreferenceProvider
import co.electriccoin.zcash.preference.model.entry.PreferenceKey
import co.electriccoin.zcash.ui.common.model.EphemeralAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

interface EphemeralAddressStorageProvider {
    fun observe(uuid: AccountUuid): Flow<EphemeralAddress?>

    suspend fun get(uuid: AccountUuid): EphemeralAddress?

    suspend fun store(uuid: AccountUuid, address: EphemeralAddress)

    suspend fun remove(uuid: AccountUuid)

    fun observeAll(uuid: AccountUuid): Flow<List<EphemeralAddress>>

    suspend fun getAll(uuid: AccountUuid): List<EphemeralAddress>

    suspend fun storeAll(uuid: AccountUuid, addresses: List<EphemeralAddress>)
}

class EphemeralAddressStorageProviderImpl(
    encryptedPreferenceProvider: EncryptedPreferenceProvider
) : EphemeralAddressStorageProvider {
    private val default = EphemeralAddressPreferenceDefault(encryptedPreferenceProvider)
    private val listDefault = EphemeralAddressListPreferenceDefault(encryptedPreferenceProvider)

    override fun observe(uuid: AccountUuid) = default.observe(uuid)

    override suspend fun get(uuid: AccountUuid): EphemeralAddress? = default.getValue(uuid)

    override suspend fun store(uuid: AccountUuid, address: EphemeralAddress) = default.putValue(address, uuid)

    override suspend fun remove(uuid: AccountUuid) = default.remove(uuid)

    override fun observeAll(uuid: AccountUuid): Flow<List<EphemeralAddress>> = listDefault.observe(uuid)

    override suspend fun getAll(uuid: AccountUuid): List<EphemeralAddress> = listDefault.getValue(uuid)

    override suspend fun storeAll(uuid: AccountUuid, addresses: List<EphemeralAddress>) =
        listDefault.putValue(addresses, uuid)
}

private class EphemeralAddressPreferenceDefault(
    private val encryptedPreferenceProvider: EncryptedPreferenceProvider
) {
    fun observe(uuid: AccountUuid): Flow<EphemeralAddress?> =
        flow { emitAll(encryptedPreferenceProvider().observe(key = getKey(uuid)).map { it?.decode() }) }

    suspend fun getValue(uuid: AccountUuid): EphemeralAddress? =
        encryptedPreferenceProvider().getString(key = getKey(uuid))?.decode()

    suspend fun putValue(address: EphemeralAddress?, uuid: AccountUuid) =
        encryptedPreferenceProvider().putString(key = getKey(uuid), value = address?.encode())

    suspend fun remove(uuid: AccountUuid) = encryptedPreferenceProvider().remove(key = getKey(uuid))

    @OptIn(ExperimentalStdlibApi::class)
    private fun getKey(uuid: AccountUuid) = PreferenceKey("ephemeral_address_${uuid.value.toHexString()}")
}

private class EphemeralAddressListPreferenceDefault(
    private val encryptedPreferenceProvider: EncryptedPreferenceProvider
) {
    fun observe(uuid: AccountUuid): Flow<List<EphemeralAddress>> =
        flow {
            emitAll(
                encryptedPreferenceProvider().observe(key = getKey(uuid)).map { it.decodeList() }
            )
        }

    suspend fun getValue(uuid: AccountUuid): List<EphemeralAddress> =
        encryptedPreferenceProvider().getString(key = getKey(uuid)).decodeList()

    suspend fun putValue(addresses: List<EphemeralAddress>, uuid: AccountUuid) =
        encryptedPreferenceProvider().putString(key = getKey(uuid), value = addresses.encodeList())

    @OptIn(ExperimentalStdlibApi::class)
    private fun getKey(uuid: AccountUuid) = PreferenceKey("ephemeral_addresses_list_${uuid.value.toHexString()}")
}

private fun EphemeralAddress?.encode(): String? = if (this == null) null else "$address.$gapPosition.$gapLimit"

private fun String?.decode(): EphemeralAddress? =
    this?.split(".")?.let {
        EphemeralAddress(
            address = it[0],
            gapPosition = it[1].toUInt(),
            gapLimit = it[2].toUInt()
        )
    }

private fun List<EphemeralAddress>.encodeList(): String? =
    if (isEmpty()) {
        null
    } else {
        joinToString(";") {
            val safeName = it.name.replace(";", "").replace("|", "")
            "${it.address}|${it.gapPosition}|${it.gapLimit}|$safeName"
        }
    }

private fun String?.decodeList(): List<EphemeralAddress> =
    if (this.isNullOrEmpty()) {
        emptyList()
    } else {
        split(";").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 3) {
                EphemeralAddress(
                    address = parts[0],
                    gapPosition = parts[1].toUIntOrNull() ?: return@mapNotNull null,
                    gapLimit = parts[2].toUIntOrNull() ?: return@mapNotNull null,
                    name = parts.getOrElse(3) { "" },
                )
            } else {
                // Backward compat: try old dot-separated format without name
                entry.decode()
            }
        }
    }
