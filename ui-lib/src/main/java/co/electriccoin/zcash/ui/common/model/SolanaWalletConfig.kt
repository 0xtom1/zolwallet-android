package co.electriccoin.zcash.ui.common.model

import kotlinx.serialization.Serializable

@Serializable
data class SolanaWalletConfig(
    val wallets: List<SolanaWalletEntry> = listOf(SolanaWalletEntry(accountIndex = 0, name = "Wallet 1")),
    val selectedAccountIndex: Int = 0,
) {
    companion object {
        const val MAX_WALLETS = 20
    }
}

@Serializable
data class SolanaWalletEntry(
    val accountIndex: Int,
    val name: String,
    val isActive: Boolean = true,
)
