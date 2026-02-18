package xyz.zolapp.solana.usecase

import xyz.zolapp.solana.repository.SolanaRepository

/**
 * Validates whether a string is a valid Solana address.
 */
class ValidateSolanaAddressUseCase(
    private val repository: SolanaRepository
) {
    operator fun invoke(address: String): Boolean = repository.isValidAddress(address)
}
