package co.electriccoin.zcash.ui.common.repository

import co.electriccoin.zcash.spackle.Twig
import co.electriccoin.zcash.ui.common.datasource.AccountDataSource
import co.electriccoin.zcash.ui.common.model.EphemeralAddress
import co.electriccoin.zcash.ui.common.provider.EphemeralAddressStorageProvider
import co.electriccoin.zcash.ui.common.provider.SynchronizerProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface EphemeralAddressRepository {
    fun observe(): Flow<EphemeralAddress?>

    suspend fun get(): EphemeralAddress?

    suspend fun create(name: String = ""): EphemeralAddress

    suspend fun invalidate()

    fun observeAll(): Flow<List<EphemeralAddress>>

    suspend fun getAll(): List<EphemeralAddress>

    suspend fun rename(address: String, newName: String)
}

class EphemeralAddressRepositoryImpl(
    private val accountDataSource: AccountDataSource,
    private val synchronizerProvider: SynchronizerProvider,
    private val ephemeralAddressStorageProvider: EphemeralAddressStorageProvider,
) : EphemeralAddressRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(): Flow<EphemeralAddress?> =
        accountDataSource
            .selectedAccount
            .map {
                it?.sdkAccount?.accountUuid
            }.distinctUntilChanged()
            .flatMapLatest { uuid ->
                if (uuid != null) ephemeralAddressStorageProvider.observe(uuid) else flowOf(null)
            }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAll(): Flow<List<EphemeralAddress>> =
        accountDataSource
            .selectedAccount
            .map {
                it?.sdkAccount?.accountUuid
            }.distinctUntilChanged()
            .flatMapLatest { uuid ->
                if (uuid != null) ephemeralAddressStorageProvider.observeAll(uuid) else flowOf(emptyList())
            }.distinctUntilChanged()

    override suspend fun invalidate() {
        val account = accountDataSource.getSelectedAccount()
        val existing = ephemeralAddressStorageProvider.get(account.sdkAccount.accountUuid)
        if (existing != null) {
            Twig.debug { "Invalidating ephemeral address $existing" }
        }
        ephemeralAddressStorageProvider.remove(account.sdkAccount.accountUuid)
    }

    override suspend fun get(): EphemeralAddress? {
        val account = accountDataSource.getSelectedAccount()
        return ephemeralAddressStorageProvider.get(account.sdkAccount.accountUuid)
    }

    override suspend fun getAll(): List<EphemeralAddress> {
        val account = accountDataSource.getSelectedAccount()
        return ephemeralAddressStorageProvider.getAll(account.sdkAccount.accountUuid)
    }

    override suspend fun create(name: String): EphemeralAddress {
        val account = accountDataSource.getSelectedAccount()
        val uuid = account.sdkAccount.accountUuid
        val new =
            synchronizerProvider
                .getSynchronizer()
                .getSingleUseTransparentAddress(uuid)
                .let {
                    EphemeralAddress(
                        address = it.address,
                        gapPosition = it.gapPosition,
                        gapLimit = it.gapLimit,
                        name = name
                    )
                }

        Twig.debug { "Generated new ephemeral address: $new" }
        ephemeralAddressStorageProvider.store(uuid, new)

        // Also append to the list
        val all = ephemeralAddressStorageProvider.getAll(uuid)
        ephemeralAddressStorageProvider.storeAll(uuid, all + new)

        return new
    }

    override suspend fun rename(address: String, newName: String) {
        val account = accountDataSource.getSelectedAccount()
        val uuid = account.sdkAccount.accountUuid
        val all = ephemeralAddressStorageProvider.getAll(uuid)
        val updated = all.map { if (it.address == address) it.copy(name = newName) else it }
        ephemeralAddressStorageProvider.storeAll(uuid, updated)
    }
}
