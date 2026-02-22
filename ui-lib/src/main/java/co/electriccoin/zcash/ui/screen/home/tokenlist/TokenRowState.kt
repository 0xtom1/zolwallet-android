package co.electriccoin.zcash.ui.screen.home.tokenlist

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

@Immutable
data class TokenRowState(
    val id: String,
    @DrawableRes val iconRes: Int?,
    val iconUrl: String?,
    val name: String,
    val ticker: String,
    val balance: String,
    val usdValue: String?,
)

@Immutable
data class TokenListState(
    val tokens: List<TokenRowState>,
    val totalPortfolioUsd: String?,
    val isLoading: Boolean,
)
