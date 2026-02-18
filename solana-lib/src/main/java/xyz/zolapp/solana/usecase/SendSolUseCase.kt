package xyz.zolapp.solana.usecase

import xyz.zolapp.solana.repository.SolanaRepository

/**
 * Sends SOL to a recipient address.
 */
class SendSolUseCase(
    private val repository: SolanaRepository
) {
    /**
     * @param recipientAddress Base58 Solana address
     * @param lamports amount in lamports (1 SOL = 1_000_000_000 lamports)
     * @return Result containing the transaction signature
     */
    suspend operator fun invoke(recipientAddress: String, lamports: Long): Result<String> =
        repository.sendSol(recipientAddress, lamports)
}
