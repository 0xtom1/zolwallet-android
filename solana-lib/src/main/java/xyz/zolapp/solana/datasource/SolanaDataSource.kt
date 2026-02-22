package xyz.zolapp.solana.datasource

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sol4k.PublicKey
import xyz.zolapp.solana.crypto.SolanaKeypairProvider
import xyz.zolapp.solana.model.SplTokenInfo
import xyz.zolapp.solana.rpc.SolanaRpcProvider

/**
 * Account info for Solana: address + balance.
 */
data class SolanaAccountInfo(
    val address: String,
    val lamports: Long,
) {
    /**
     * Balance in SOL (1 SOL = 1_000_000_000 lamports).
     */
    val solBalance: Double get() = lamports / 1_000_000_000.0
}

/**
 * Provides on-demand Solana account data (balance, tokens, prices).
 * Data is fetched only when explicitly requested — no background polling.
 */
class SolanaDataSource(
    private val keypairProvider: SolanaKeypairProvider,
    private val rpcProvider: SolanaRpcProvider,
) {
    private val _accountInfo = MutableStateFlow<SolanaAccountInfo?>(null)
    val accountInfo: StateFlow<SolanaAccountInfo?> = _accountInfo.asStateFlow()

    private val _splTokens = MutableStateFlow<List<SplTokenInfo>>(emptyList())
    val splTokens: StateFlow<List<SplTokenInfo>> = _splTokens.asStateFlow()

    private val _solTokenInfo = MutableStateFlow<SplTokenInfo?>(null)
    val solTokenInfo: StateFlow<SplTokenInfo?> = _solTokenInfo.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    /**
     * Fetches all token data (SOL balance, SPL tokens, prices) via the Helius DAS API.
     * Call this on screen load or user-triggered refresh.
     *
     * @param accountIndex the Solana account index for key derivation
     */
    suspend fun fetchTokenData(accountIndex: Int = 0) {
        try {
            val address = keypairProvider.getAddress(accountIndex)
            val publicKey = PublicKey(address)
            val (tokens, solToken) = rpcProvider.getAssetsByOwner(publicKey)
            _splTokens.value = tokens

            if (solToken != null) {
                _solTokenInfo.value = solToken
                _accountInfo.value = SolanaAccountInfo(
                    address = address,
                    lamports = solToken.balance
                )
            } else {
                // Fallback to direct balance query if DAS doesn't return native balance
                val lamports = rpcProvider.getBalance(publicKey)
                _accountInfo.value = SolanaAccountInfo(
                    address = address,
                    lamports = lamports
                )
            }
            _error.value = null
        } catch (e: Exception) {
            _error.value = e
        }
    }

    /**
     * Fetches only the native SOL balance (lighter than fetchTokenData).
     *
     * @param accountIndex the Solana account index for key derivation
     */
    suspend fun refreshBalance(accountIndex: Int = 0) {
        try {
            val address = keypairProvider.getAddress(accountIndex)
            val publicKey = PublicKey(address)
            val lamports = rpcProvider.getBalance(publicKey)
            _accountInfo.value = SolanaAccountInfo(
                address = address,
                lamports = lamports
            )
            _error.value = null
        } catch (e: Exception) {
            _error.value = e
        }
    }
}
