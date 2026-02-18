package xyz.zolapp.solana.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.sol4k.PublicKey
import org.sol4k.Transaction
import org.sol4k.instruction.TransferInstruction
import xyz.zolapp.solana.crypto.SolanaKeypairProvider
import xyz.zolapp.solana.datasource.SolanaAccountInfo
import xyz.zolapp.solana.datasource.SolanaDataSource
import xyz.zolapp.solana.rpc.SolanaRpcProvider

/**
 * Repository for Solana wallet operations: balance, send, address.
 */
class SolanaRepository(
    private val keypairProvider: SolanaKeypairProvider,
    private val rpcProvider: SolanaRpcProvider,
    private val dataSource: SolanaDataSource,
) {
    /**
     * Observable account info (address + balance).
     */
    val accountInfo: StateFlow<SolanaAccountInfo?> = dataSource.accountInfo

    /**
     * Observable error state.
     */
    val error: StateFlow<Throwable?> = dataSource.error

    /**
     * Returns the Solana wallet address.
     */
    suspend fun getAddress(): String = keypairProvider.getAddress()

    /**
     * Starts background balance polling.
     */
    fun startBalancePolling() = dataSource.startPolling()

    /**
     * Refreshes the balance once.
     */
    suspend fun refreshBalance() = dataSource.refreshBalance()

    /**
     * Sends SOL to the given recipient.
     *
     * @param recipientAddress Base58 Solana address
     * @param lamports amount in lamports (1 SOL = 1_000_000_000 lamports)
     * @return Result containing the transaction signature on success
     */
    suspend fun sendSol(recipientAddress: String, lamports: Long): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val keypair = keypairProvider.getKeypair()
                val fromPublicKey = keypair.publicKey
                val toPublicKey = PublicKey(recipientAddress)
                val blockhash = rpcProvider.getLatestBlockhash()

                val instruction = TransferInstruction(
                    fromPublicKey,
                    toPublicKey,
                    lamports = lamports
                )

                val transaction = Transaction(
                    blockhash,
                    listOf(instruction),
                    fromPublicKey
                )

                transaction.sign(keypair)

                rpcProvider.sendTransaction(transaction)
            }
        }

    /**
     * Validates whether the given string is a valid Solana address.
     */
    fun isValidAddress(address: String): Boolean = rpcProvider.isValidAddress(address)
}
