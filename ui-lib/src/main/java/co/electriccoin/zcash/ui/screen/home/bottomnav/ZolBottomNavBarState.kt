package co.electriccoin.zcash.ui.screen.home.bottomnav

import androidx.compose.runtime.Immutable

@Immutable
data class ZolBottomNavBarState(
    val selectedTab: BottomNavTab,
    val onPortfolioClick: () -> Unit,
    val onWalletsClick: () -> Unit,
    val onBridgeClick: () -> Unit,
    val onSwapClick: () -> Unit,
    val onPayClick: () -> Unit,
)

enum class BottomNavTab {
    PORTFOLIO,
    WALLETS,
    BRIDGE,
    SWAP,
    PAY,
}
