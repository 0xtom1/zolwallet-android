package xyz.zolapp.solana.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sol4k.PublicKey
import xyz.zolapp.solana.crypto.SolanaKeypairProvider
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
 * Polls the Solana RPC for balance updates.
 */
class SolanaDataSource(
    private val keypairProvider: SolanaKeypairProvider,
    private val rpcProvider: SolanaRpcProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _accountInfo = MutableStateFlow<SolanaAccountInfo?>(null)
    val accountInfo: StateFlow<SolanaAccountInfo?> = _accountInfo.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    /**
     * Starts polling for balance. Safe to call multiple times.
     */
    fun startPolling(intervalMs: Long = POLL_INTERVAL_MS) {
        scope.launch {
            val address = keypairProvider.getAddress()
            val publicKey = PublicKey(address)

            while (true) {
                try {
                    val lamports = rpcProvider.getBalance(publicKey)
                    _accountInfo.value = SolanaAccountInfo(
                        address = address,
                        lamports = lamports
                    )
                    _error.value = null
                } catch (e: Exception) {
                    _error.value = e
                }
                delay(intervalMs)
            }
        }
    }

    /**
     * Fetches the balance once (non-polling).
     */
    suspend fun refreshBalance() {
        try {
            val address = keypairProvider.getAddress()
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

    companion object {
        const val POLL_INTERVAL_MS = 15_000L
    }
}
