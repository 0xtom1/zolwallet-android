package xyz.zolapp.solana.usecase

import xyz.zolapp.solana.repository.SolanaRepository

/**
 * Returns the Solana wallet address.
 */
class GetSolanaAddressUseCase(
    private val repository: SolanaRepository
) {
    suspend operator fun invoke(accountIndex: Int = 0): String = repository.getAddress(accountIndex)
}
