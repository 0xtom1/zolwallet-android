@file:Suppress("ktlint:standard:filename")

package co.electriccoin.zcash.ui.screen.solanareceive

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.design.component.QrState
import co.electriccoin.zcash.ui.design.component.ZashiButton
import co.electriccoin.zcash.ui.design.component.ZashiButtonDefaults
import co.electriccoin.zcash.ui.design.component.ZashiQr
import co.electriccoin.zcash.ui.design.component.ZashiSmallTopAppBar
import co.electriccoin.zcash.ui.design.component.ZashiTopAppBarBackNavigation
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors
import co.electriccoin.zcash.ui.design.theme.typography.ZashiTypography
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SolanaReceiveScreen() {
    val viewModel = koinViewModel<SolanaReceiveVM>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler { state.onBack() }

    SolanaReceiveView(state = state)
}

@Composable
private fun SolanaReceiveView(state: SolanaReceiveState) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ZashiSmallTopAppBar(
            title = stringResource(R.string.solana_receive_title),
            navigationAction = { ZashiTopAppBarBackNavigation(onBack = state.onBack) }
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // QR Code
                state.address?.let { addr ->
                    ZashiQr(
                        state = QrState(qrData = addr),
                        modifier = Modifier.size(240.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Address label
                Text(
                    text = stringResource(R.string.solana_receive_address_label),
                    style = ZashiTypography.textSm,
                    color = ZashiColors.Text.textTertiary,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Address
                Text(
                    text = state.address.orEmpty(),
                    style = ZashiTypography.textMd,
                    color = ZashiColors.Text.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.weight(1f))

                // Copy button
                ZashiButton(
                    onClick = state.onCopyClick,
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.solana_receive_copy),
                    colors = ZashiButtonDefaults.primaryColors()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Serializable
data object SolanaReceiveArgs
