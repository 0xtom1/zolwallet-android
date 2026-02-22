package co.electriccoin.zcash.ui.common.provider

import co.electriccoin.zcash.preference.EncryptedPreferenceProvider
import co.electriccoin.zcash.preference.api.PreferenceProvider
import co.electriccoin.zcash.preference.model.entry.PreferenceDefault
import co.electriccoin.zcash.preference.model.entry.PreferenceKey
import co.electriccoin.zcash.ui.common.model.SolanaWalletConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

interface SolanaWalletStorageProvider {
    fun observe(): Flow<SolanaWalletConfig>

    suspend fun get(): SolanaWalletConfig

    suspend fun store(config: SolanaWalletConfig)
}

class SolanaWalletStorageProviderImpl(
    private val encryptedPreferenceProvider: EncryptedPreferenceProvider
) : SolanaWalletStorageProvider {
    private val default = SolanaWalletConfigPreferenceDefault()

    override fun observe(): Flow<SolanaWalletConfig> =
        flow { emitAll(default.observe(encryptedPreferenceProvider())) }

    override suspend fun get(): SolanaWalletConfig =
        default.getValue(encryptedPreferenceProvider())

    override suspend fun store(config: SolanaWalletConfig) {
        default.putValue(encryptedPreferenceProvider(), config)
    }
}

private class SolanaWalletConfigPreferenceDefault : PreferenceDefault<SolanaWalletConfig> {
    override val key: PreferenceKey = PreferenceKey("solana_wallet_config")

    override suspend fun getValue(preferenceProvider: PreferenceProvider): SolanaWalletConfig {
        val json = preferenceProvider.getString(key) ?: return SolanaWalletConfig()
        return try {
            Json.decodeFromString<SolanaWalletConfig>(json)
        } catch (_: Exception) {
            SolanaWalletConfig()
        }
    }

    override suspend fun putValue(
        preferenceProvider: PreferenceProvider,
        newValue: SolanaWalletConfig
    ) {
        preferenceProvider.putString(key, Json.encodeToString(SolanaWalletConfig.serializer(), newValue))
    }
}
