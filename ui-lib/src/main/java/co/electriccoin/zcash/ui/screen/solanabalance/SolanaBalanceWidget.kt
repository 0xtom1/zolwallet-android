package co.electriccoin.zcash.ui.screen.solanabalance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.design.component.ZashiButton
import co.electriccoin.zcash.ui.design.component.ZashiButtonDefaults
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors
import co.electriccoin.zcash.ui.design.theme.typography.ZashiTypography

@Composable
fun SolanaBalanceWidget(
    state: SolanaBalanceWidgetState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ZashiColors.Surfaces.bgSecondary)
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.solana_balance_label),
            style = ZashiTypography.textXs,
            color = ZashiColors.Text.textTertiary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text =
                if (state.isLoading) {
                    stringResource(R.string.solana_balance_loading)
                } else {
                    stringResource(R.string.solana_balance_sol, "%.9f".format(state.solBalance ?: 0.0))
                },
            style = ZashiTypography.textLg,
            color = ZashiColors.Text.textPrimary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            ZashiButton(
                onClick = state.onReceiveClick,
                text = stringResource(R.string.home_button_solana_receive),
                colors = ZashiButtonDefaults.tertiaryColors(),
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            ZashiButton(
                onClick = state.onSendClick,
                text = stringResource(R.string.home_button_solana_send),
                colors = ZashiButtonDefaults.tertiaryColors(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
