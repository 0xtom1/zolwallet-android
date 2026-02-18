package xyz.zolapp.solana.usecase

import kotlinx.coroutines.flow.StateFlow
import xyz.zolapp.solana.datasource.SolanaAccountInfo
import xyz.zolapp.solana.repository.SolanaRepository

/**
 * Observes the current Solana account balance.
 */
class GetSolanaBalanceUseCase(
    private val repository: SolanaRepository
) {
    /**
     * Returns a [StateFlow] of the current account info (address + balance).
     */
    fun observe(): StateFlow<SolanaAccountInfo?> = repository.accountInfo

    /**
     * Forces a one-time balance refresh.
     */
    suspend fun refresh() = repository.refreshBalance()
}
