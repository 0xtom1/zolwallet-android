package co.electriccoin.zcash.ui.screen.home.bottomnav

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors

@Composable
fun ZolBottomNavBar(state: ZolBottomNavBarState) {
    NavigationBar(
        containerColor = ZashiColors.Surfaces.bgPrimary,
    ) {
        NavigationBarItem(
            selected = state.selectedTab == BottomNavTab.PORTFOLIO,
            onClick = state.onPortfolioClick,
            icon = { Icon(painterResource(R.drawable.ic_nav_portfolio), contentDescription = null) },
            label = { Text("Portfolio") },
            colors = navItemColors(),
        )
        NavigationBarItem(
            selected = state.selectedTab == BottomNavTab.WALLETS,
            onClick = state.onWalletsClick,
            icon = { Icon(painterResource(R.drawable.ic_nav_wallets), contentDescription = null) },
            label = { Text("Wallets") },
            colors = navItemColors(),
        )
        NavigationBarItem(
            selected = state.selectedTab == BottomNavTab.BRIDGE,
            onClick = state.onBridgeClick,
            icon = { Icon(painterResource(R.drawable.ic_nav_bridge), contentDescription = null) },
            label = { Text("Bridge") },
            colors = navItemColors(),
        )
        NavigationBarItem(
            selected = state.selectedTab == BottomNavTab.SWAP,
            onClick = state.onSwapClick,
            icon = { Icon(painterResource(R.drawable.ic_home_swap), contentDescription = null) },
            label = { Text("Swap") },
            colors = navItemColors(),
        )
        NavigationBarItem(
            selected = state.selectedTab == BottomNavTab.PAY,
            onClick = state.onPayClick,
            icon = { Icon(painterResource(R.drawable.ic_home_pay), contentDescription = null) },
            label = { Text("Pay") },
            colors = navItemColors(),
        )
    }
}

@Composable
private fun navItemColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = ZashiColors.Text.textPrimary,
        selectedTextColor = ZashiColors.Text.textPrimary,
        unselectedIconColor = ZashiColors.Text.textTertiary,
        unselectedTextColor = ZashiColors.Text.textTertiary,
        indicatorColor = ZashiColors.Surfaces.bgSecondary,
    )
