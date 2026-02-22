package co.electriccoin.zcash.ui.screen.solanabalance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.sdk.ANDROID_STATE_FLOW_TIMEOUT
import co.electriccoin.zcash.ui.NavigationRouter
import co.electriccoin.zcash.ui.common.datasource.SolanaWalletDataSource
import co.electriccoin.zcash.ui.screen.solanareceive.SolanaReceiveArgs
import co.electriccoin.zcash.ui.screen.solanasend.SolanaSendArgs
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import xyz.zolapp.solana.datasource.SolanaAccountInfo
import xyz.zolapp.solana.repository.SolanaRepository

data class SolanaBalanceWidgetState(
    val solBalance: Double?,
    val address: String?,
    val isLoading: Boolean,
    val onSendClick: () -> Unit,
    val onReceiveClick: () -> Unit,
)

class SolanaBalanceWidgetVM(
    private val solanaRepository: SolanaRepository,
    private val navigationRouter: NavigationRouter,
    private val solanaWalletDataSource: SolanaWalletDataSource,
) : ViewModel() {

    init {
        viewModelScope.launch {
            solanaWalletDataSource.selectedAccountIndex.collectLatest { index ->
                solanaRepository.fetchTokenData(index)
            }
        }
    }

    val state: StateFlow<SolanaBalanceWidgetState> =
        solanaRepository.accountInfo
            .map { info -> createState(info) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
                initialValue = createState(null)
            )

    private fun createState(info: SolanaAccountInfo?) =
        SolanaBalanceWidgetState(
            solBalance = info?.solBalance,
            address = info?.address,
            isLoading = info == null,
            onSendClick = ::onSendClick,
            onReceiveClick = ::onReceiveClick,
        )

    private fun onSendClick() = navigationRouter.forward(SolanaSendArgs)

    private fun onReceiveClick() = navigationRouter.forward(SolanaReceiveArgs)
}
