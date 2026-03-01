package co.electriccoin.zcash.ui.screen.swap

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.ui.screen.home.bottomnav.BottomNavTab
import co.electriccoin.zcash.ui.screen.home.bottomnav.ZolBottomNavBarForTab
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Composable
fun SwapScreen(showBottomBar: Boolean = true) {
    val vm = koinViewModel<SwapVM>()
    val state by vm.state.collectAsStateWithLifecycle()
    val cancelState by vm.cancelState.collectAsStateWithLifecycle()
    state?.let {
        SwapView(
            state = it,
            bottomBar = if (showBottomBar) ({ ZolBottomNavBarForTab(BottomNavTab.BRIDGE) }) else ({}),
        )
    }
    BackHandler(state != null) { state?.onBack?.invoke() }
    SwapCancelView(cancelState)
}

@Serializable
data object SwapArgs
