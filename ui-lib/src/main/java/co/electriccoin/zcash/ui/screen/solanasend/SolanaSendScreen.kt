@file:Suppress("ktlint:standard:filename")

package co.electriccoin.zcash.ui.screen.solanasend

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.design.component.ZashiButton
import co.electriccoin.zcash.ui.design.component.ZashiButtonDefaults
import co.electriccoin.zcash.ui.design.component.ZashiTextField
import co.electriccoin.zcash.ui.design.component.ZashiSmallTopAppBar
import co.electriccoin.zcash.ui.design.component.ZashiTopAppBarBackNavigation
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors
import co.electriccoin.zcash.ui.design.theme.typography.ZashiTypography
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SolanaSendScreen() {
    val viewModel = koinViewModel<SolanaSendViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler { state.onBack() }

    SolanaSendView(state = state)
}

@Composable
private fun SolanaSendView(state: SolanaSendState) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
    ) {
        ZashiSmallTopAppBar(
            title = stringResource(R.string.solana_send_title),
            navigationAction = { ZashiTopAppBarBackNavigation(onBack = state.onBack) }
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Available balance
            if (state.availableBalance != null) {
                Text(
                    text = stringResource(R.string.solana_balance_sol, "%.9f".format(state.availableBalance)),
                    style = ZashiTypography.textSm,
                    color = ZashiColors.Text.textTertiary,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recipient address
            Text(
                text = stringResource(R.string.solana_send_to_label),
                style = ZashiTypography.textSm,
                color = ZashiColors.Text.textTertiary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            ZashiTextField(
                value = state.recipientAddress,
                onValueChange = state.onRecipientChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.solana_send_to_hint)) },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount
            Text(
                text = stringResource(R.string.solana_send_amount_label),
                style = ZashiTypography.textSm,
                color = ZashiColors.Text.textTertiary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            ZashiTextField(
                value = state.amount,
                onValueChange = state.onAmountChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.solana_send_amount_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Error
            if (state.error != null) {
                Text(
                    text = state.error,
                    style = ZashiTypography.textSm,
                    color = ZashiColors.Text.textError,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Success
            if (state.successSignature != null) {
                Text(
                    text = stringResource(R.string.solana_send_success),
                    style = ZashiTypography.textSm,
                    color = ZashiColors.Text.textPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Send button
            ZashiButton(
                onClick = state.onSendClick,
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(
                    if (state.isSending) R.string.solana_send_sending else R.string.solana_send_button
                ),
                enabled = !state.isSending,
                colors = ZashiButtonDefaults.primaryColors()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Serializable
data object SolanaSendArgs
