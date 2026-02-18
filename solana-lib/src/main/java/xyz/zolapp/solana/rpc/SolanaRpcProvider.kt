package xyz.zolapp.solana.rpc

import org.sol4k.Connection
import org.sol4k.PublicKey
import org.sol4k.Transaction
import xyz.zolapp.solana.BuildConfig

/**
 * Wraps Sol4k [Connection] for Solana RPC calls.
 *
 * @param rpcUrl RPC endpoint URL. Defaults to mainnet.
 */
class SolanaRpcProvider(
    private val rpcUrl: String = MAINNET_RPC_URL
) {
    private val connection by lazy { Connection(rpcUrl) }

    /**
     * Returns the SOL balance in lamports for the given public key.
     */
    fun getBalance(publicKey: PublicKey): Long = connection.getBalance(publicKey).toLong()

    /**
     * Returns the latest blockhash string.
     */
    fun getLatestBlockhash(): String = connection.getLatestBlockhash()

    /**
     * Sends a signed transaction and returns the transaction signature.
     */
    fun sendTransaction(transaction: Transaction): String = connection.sendTransaction(transaction)

    /**
     * Checks if the given address is a valid Solana public key (Base58, 32 bytes).
     */
    fun isValidAddress(address: String): Boolean =
        try {
            val decoded = PublicKey(address)
            decoded.toBase58() == address
        } catch (_: Exception) {
            false
        }

    companion object {
        private val HELIUS_API_KEY = BuildConfig.HELIUS_API_KEY
        val MAINNET_RPC_URL = "https://mainnet.helius-rpc.com/?api-key=$HELIUS_API_KEY"
        val DEVNET_RPC_URL = "https://devnet.helius-rpc.com/?api-key=$HELIUS_API_KEY"
    }
}
