package co.electriccoin.zcash.ui.screen.solanasend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.sdk.ANDROID_STATE_FLOW_TIMEOUT
import co.electriccoin.zcash.ui.NavigationRouter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.zolapp.solana.usecase.GetSolanaBalanceUseCase
import xyz.zolapp.solana.usecase.SendSolUseCase
import xyz.zolapp.solana.usecase.ValidateSolanaAddressUseCase

data class SolanaSendState(
    val recipientAddress: String,
    val amount: String,
    val availableBalance: Double?,
    val isSending: Boolean,
    val error: String?,
    val successSignature: String?,
    val onRecipientChanged: (String) -> Unit,
    val onAmountChanged: (String) -> Unit,
    val onSendClick: () -> Unit,
    val onBack: () -> Unit,
)

class SolanaSendViewModel(
    private val getSolanaBalance: GetSolanaBalanceUseCase,
    private val sendSol: SendSolUseCase,
    private val validateAddress: ValidateSolanaAddressUseCase,
    private val navigationRouter: NavigationRouter,
) : ViewModel() {

    private val recipientAddress = MutableStateFlow("")
    private val amount = MutableStateFlow("")
    private val isSending = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val successSignature = MutableStateFlow<String?>(null)

    val state: StateFlow<SolanaSendState> =
        combine(
            recipientAddress,
            amount,
            getSolanaBalance.observe(),
            isSending,
            error,
            successSignature,
        ) { values ->
            @Suppress("MagicNumber")
            SolanaSendState(
                recipientAddress = values[0] as String,
                amount = values[1] as String,
                availableBalance = (values[2] as? xyz.zolapp.solana.datasource.SolanaAccountInfo)?.solBalance,
                isSending = values[3] as Boolean,
                error = values[4] as? String,
                successSignature = values[5] as? String,
                onRecipientChanged = ::onRecipientChanged,
                onAmountChanged = ::onAmountChanged,
                onSendClick = ::onSendClick,
                onBack = ::onBack,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
            initialValue = SolanaSendState(
                recipientAddress = "",
                amount = "",
                availableBalance = null,
                isSending = false,
                error = null,
                successSignature = null,
                onRecipientChanged = ::onRecipientChanged,
                onAmountChanged = ::onAmountChanged,
                onSendClick = ::onSendClick,
                onBack = ::onBack,
            )
        )

    private fun onRecipientChanged(value: String) {
        recipientAddress.value = value
        error.value = null
        successSignature.value = null
    }

    private fun onAmountChanged(value: String) {
        amount.value = value
        error.value = null
        successSignature.value = null
    }

    @Suppress("MagicNumber")
    private fun onSendClick() {
        if (isSending.value) return

        val address = recipientAddress.value.trim()
        val amountStr = amount.value.trim()

        if (!validateAddress(address)) {
            error.value = "Invalid Solana address"
            return
        }

        val solAmount = amountStr.toDoubleOrNull()
        if (solAmount == null || solAmount <= 0) {
            error.value = "Invalid amount"
            return
        }

        val lamports = (solAmount * 1_000_000_000).toLong()

        val balance = getSolanaBalance.observe().value
        if (balance != null && lamports > balance.lamports) {
            error.value = "Insufficient balance"
            return
        }

        isSending.value = true
        error.value = null

        viewModelScope.launch {
            val result = sendSol(address, lamports)
            isSending.value = false

            result.fold(
                onSuccess = { signature ->
                    successSignature.value = signature
                    getSolanaBalance.refresh()
                },
                onFailure = { throwable ->
                    error.value = throwable.message ?: "Failed to send transaction"
                }
            )
        }
    }

    private fun onBack() = navigationRouter.back()
}
