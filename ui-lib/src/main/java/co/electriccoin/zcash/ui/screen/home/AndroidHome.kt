@file:Suppress("ktlint:standard:filename")

package co.electriccoin.zcash.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.di.koinActivityViewModel
import co.electriccoin.zcash.ui.common.appbar.ZashiTopAppBarVM
import co.electriccoin.zcash.ui.screen.home.tokenlist.TokenListVM
import co.electriccoin.zcash.ui.screen.restoresuccess.WrapRestoreSuccess
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AndroidHome() {
    val topAppBarVM = koinActivityViewModel<ZashiTopAppBarVM>()
    val homeVM = koinViewModel<HomeVM>()
    val tokenListVM = koinViewModel<TokenListVM>()
    val restoreDialogState by homeVM.restoreDialogState.collectAsStateWithLifecycle()
    val appBarState by topAppBarVM.state.collectAsStateWithLifecycle()
    val state by homeVM.state.collectAsStateWithLifecycle()
    homeVM.uiLifecyclePipeline.collectAsStateWithLifecycle()
    val tokenListState by tokenListVM.state.collectAsStateWithLifecycle()

    state?.let {
        HomeView(
            appBarState = appBarState,
            state = it,
            tokenListState = tokenListState,
        )
    }

    if (restoreDialogState != null) {
        WrapRestoreSuccess()
    }
}

@Serializable
object HomeArgs
