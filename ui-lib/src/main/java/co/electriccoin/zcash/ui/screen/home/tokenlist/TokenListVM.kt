package co.electriccoin.zcash.ui.screen.home.tokenlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.sdk.model.Zatoshi
import cash.z.ecc.sdk.ANDROID_STATE_FLOW_TIMEOUT
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.common.datasource.AccountDataSource
import co.electriccoin.zcash.ui.common.datasource.SolanaWalletDataSource
import co.electriccoin.zcash.ui.common.wallet.ExchangeRateState
import co.electriccoin.zcash.ui.common.repository.ExchangeRateRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import co.electriccoin.zcash.ui.common.usecase.SolanaWalletContactSyncUseCase
import xyz.zolapp.solana.model.SplTokenInfo
import xyz.zolapp.solana.repository.SolanaRepository
import java.text.NumberFormat
import java.util.Locale

class TokenListVM(
    accountDataSource: AccountDataSource,
    exchangeRateRepository: ExchangeRateRepository,
    private val solanaRepository: SolanaRepository,
    private val solanaWalletDataSource: SolanaWalletDataSource,
    @Suppress("unused") walletContactSync: SolanaWalletContactSyncUseCase,
) : ViewModel() {

    init {
        viewModelScope.launch {
            solanaWalletDataSource.selectedAccountIndex.collectLatest { index ->
                while (true) {
                    solanaRepository.fetchTokenData(index)
                    delay(REFRESH_INTERVAL_MS)
                }
            }
        }
    }

    val state: StateFlow<TokenListState> =
        combine(
            accountDataSource.selectedAccount.filterNotNull(),
            exchangeRateRepository.state,
            solanaRepository.solTokenInfo,
            solanaRepository.splTokens,
        ) { account, exchangeRateState, solTokenInfo, splTokens ->
            createState(
                zecBalance = account.totalBalance,
                exchangeRateState = exchangeRateState,
                solTokenInfo = solTokenInfo,
                splTokens = splTokens,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
            initialValue = TokenListState(tokens = emptyList(), totalPortfolioUsd = null, isLoading = true)
        )

    private fun createState(
        zecBalance: Zatoshi,
        exchangeRateState: ExchangeRateState,
        solTokenInfo: SplTokenInfo?,
        splTokens: List<SplTokenInfo>,
    ): TokenListState {
        val showUsd = exchangeRateState is ExchangeRateState.Data
        val tokens = mutableListOf<TokenRowState>()
        var totalUsd = 0.0
        var hasAnyPrice = false

        // ZEC always first
        val zecAmount = zecBalance.value.toDouble() / ZATOSHI_PER_ZEC
        val zecPrice = (exchangeRateState as? ExchangeRateState.Data)?.currencyConversion?.priceOfZec
        val zecUsd = if (showUsd && zecPrice != null) {
            hasAnyPrice = true
            zecAmount * zecPrice
        } else {
            null
        }
        if (zecUsd != null) totalUsd += zecUsd

        tokens.add(
            TokenRowState(
                id = "ZEC",
                iconRes = R.drawable.ic_token_zec,
                iconUrl = null,
                name = "Zcash",
                ticker = "ZEC",
                balance = formatBalance(zecAmount, MAX_ZEC_DECIMALS),
                usdValue = zecUsd?.let { formatUsd(it) },
            )
        )

        // SOL always second
        if (solTokenInfo != null) {
            val solUsd = if (showUsd) solTokenInfo.valueUsd else null
            if (solUsd != null) {
                hasAnyPrice = true
                totalUsd += solUsd
            }
            tokens.add(
                TokenRowState(
                    id = "SOL",
                    iconRes = R.drawable.ic_token_sol,
                    iconUrl = null,
                    name = "Solana",
                    ticker = "SOL",
                    balance = formatBalance(solTokenInfo.uiAmount, MAX_SOL_DECIMALS),
                    usdValue = solUsd?.let { formatUsd(it) },
                )
            )
        }

        // SPL tokens sorted by USD value descending
        val sortedSpl = splTokens.sortedByDescending { it.valueUsd ?: 0.0 }
        for (token in sortedSpl) {
            val tokenUsd = if (showUsd) token.valueUsd else null
            if (tokenUsd != null) {
                hasAnyPrice = true
                totalUsd += tokenUsd
            }
            tokens.add(
                TokenRowState(
                    id = token.mint,
                    iconRes = null,
                    iconUrl = token.imageUrl,
                    name = token.name.ifBlank { token.symbol },
                    ticker = token.symbol,
                    balance = formatBalance(token.uiAmount, MAX_SPL_DECIMALS),
                    usdValue = tokenUsd?.let { formatUsd(it) },
                )
            )
        }

        return TokenListState(
            tokens = tokens,
            totalPortfolioUsd = if (!showUsd) null else if (hasAnyPrice) formatUsd(totalUsd) else null,
            isLoading = false,
        )
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 30_000L
        private const val ZATOSHI_PER_ZEC = 100_000_000.0
        private const val MAX_ZEC_DECIMALS = 8
        private const val MAX_SOL_DECIMALS = 4
        private const val MAX_SPL_DECIMALS = 4

        private val usdFormat = NumberFormat.getCurrencyInstance(Locale.US)

        private fun formatUsd(value: Double): String = usdFormat.format(value)

        private fun formatBalance(amount: Double, maxDecimals: Int): String {
            if (amount == 0.0) return "0"
            val formatted = String.format(Locale.US, "%.${maxDecimals}f", amount)
            // Trim trailing zeros but keep at least one decimal
            return formatted.trimEnd('0').trimEnd('.')
        }
    }
}
