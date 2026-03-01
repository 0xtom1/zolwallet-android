package co.electriccoin.zcash.ui.screen.tabhost

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import co.electriccoin.zcash.ui.design.component.BlankBgScaffold
import co.electriccoin.zcash.ui.screen.comingsoon.ComingSoonContent
import co.electriccoin.zcash.ui.screen.home.AndroidHome
import co.electriccoin.zcash.ui.screen.home.bottomnav.BottomNavTab
import co.electriccoin.zcash.ui.screen.home.bottomnav.ZolBottomNavBar
import co.electriccoin.zcash.ui.screen.home.bottomnav.ZolBottomNavBarState
import co.electriccoin.zcash.ui.screen.pay.PayScreen
import co.electriccoin.zcash.ui.screen.swap.SwapScreen
import co.electriccoin.zcash.ui.screen.wallets.WalletsScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data object TabHostArgs

private val TABS = BottomNavTab.entries.toList()

@Composable
fun TabHostScreen() {
    val pagerState = rememberPagerState(initialPage = 2, pageCount = { TABS.size })
    val scope = rememberCoroutineScope()

    val currentPage = pagerState.currentPage
    val selectedTab = TABS[currentPage]

    BackHandler(enabled = currentPage != 2) {
        scope.launch { pagerState.animateScrollToPage(2) }
    }

    BlankBgScaffold(
        bottomBar = {
            ZolBottomNavBar(
                ZolBottomNavBarState(
                    selectedTab = selectedTab,
                    onWalletsClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    onBridgeClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    onPortfolioClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                    onSwapClick = { scope.launch { pagerState.animateScrollToPage(3) } },
                    onPayClick = { scope.launch { pagerState.animateScrollToPage(4) } },
                )
            )
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 4,
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                when (page) {
                    0 -> WalletsScreen(showBottomBar = false)
                    1 -> SwapScreen(showBottomBar = false)
                    2 -> AndroidHome()
                    3 -> ComingSoonContent()
                    4 -> PayScreen(showBottomBar = false)
                }
            }
        }
    }
}
