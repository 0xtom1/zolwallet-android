package co.electriccoin.zcash.ui.screen.solanareceive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.sdk.ANDROID_STATE_FLOW_TIMEOUT
import co.electriccoin.zcash.ui.NavigationRouter
import co.electriccoin.zcash.ui.common.usecase.CopyToClipboardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.zolapp.solana.usecase.GetSolanaAddressUseCase

data class SolanaReceiveState(
    val address: String?,
    val isLoading: Boolean,
    val onCopyClick: () -> Unit,
    val onBack: () -> Unit,
)

class SolanaReceiveVM(
    private val getSolanaAddress: GetSolanaAddressUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val navigationRouter: NavigationRouter,
) : ViewModel() {

    private val address = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            address.value = getSolanaAddress()
        }
    }

    val state: StateFlow<SolanaReceiveState> =
        address
            .map { addr ->
                SolanaReceiveState(
                    address = addr,
                    isLoading = addr == null,
                    onCopyClick = { addr?.let { copyToClipboard(value = it) } },
                    onBack = ::onBack,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
                initialValue = SolanaReceiveState(
                    address = null,
                    isLoading = true,
                    onCopyClick = {},
                    onBack = ::onBack,
                )
            )

    private fun onBack() = navigationRouter.back()
}
