package co.electriccoin.zcash.ui.common.datasource

import co.electriccoin.zcash.ui.common.model.SolanaWalletConfig
import co.electriccoin.zcash.ui.common.model.SolanaWalletEntry
import co.electriccoin.zcash.ui.common.provider.SolanaWalletStorageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SolanaWalletDataSource(
    private val storageProvider: SolanaWalletStorageProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val config: StateFlow<SolanaWalletConfig> =
        storageProvider.observe()
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = SolanaWalletConfig(),
            )

    val selectedAccountIndex: StateFlow<Int> =
        config.map { it.selectedAccountIndex }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = 0,
            )

    suspend fun createWallet(name: String): Int? {
        val current = storageProvider.get()
        if (current.wallets.size >= SolanaWalletConfig.MAX_WALLETS) return null

        val nextIndex = (current.wallets.maxOfOrNull { it.accountIndex } ?: -1) + 1
        val updated = current.copy(
            wallets = current.wallets + SolanaWalletEntry(
                accountIndex = nextIndex,
                name = name,
            ),
            selectedAccountIndex = nextIndex,
        )
        storageProvider.store(updated)
        return nextIndex
    }

    suspend fun renameWallet(accountIndex: Int, newName: String) {
        val current = storageProvider.get()
        val updated = current.copy(
            wallets = current.wallets.map { entry ->
                if (entry.accountIndex == accountIndex) entry.copy(name = newName) else entry
            }
        )
        storageProvider.store(updated)
    }

    suspend fun selectWallet(accountIndex: Int) {
        val current = storageProvider.get()
        if (current.wallets.none { it.accountIndex == accountIndex }) return
        storageProvider.store(current.copy(selectedAccountIndex = accountIndex))
    }

    fun nextDefaultName(): String {
        val count = config.value.wallets.size
        return "Wallet ${count + 1}"
    }
}
