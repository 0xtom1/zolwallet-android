package co.electriccoin.zcash.ui.screen.home.tokenlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.design.theme.balances.LocalBalancesAvailable
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors

private const val HIDE_BALANCE_PLACEHOLDER = "***"

fun LazyListScope.tokenListItems(state: TokenListState) {
    if (state.isLoading) {
        item(key = "token_loading") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading...",
                    color = ZashiColors.Text.textTertiary,
                    fontSize = 14.sp,
                )
            }
        }
        return
    }

    items(
        items = state.tokens,
        key = { it.id }
    ) { token ->
        TokenRow(token = token)
    }
}

@Composable
private fun TokenRow(token: TokenRowState) {
    val balancesAvailable = LocalBalancesAvailable.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Token icon
        if (token.iconRes != null) {
            Image(
                painter = painterResource(token.iconRes),
                contentDescription = token.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
            )
        } else {
            // Placeholder circle with first letter
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ZashiColors.Surfaces.bgTertiary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = token.ticker.take(1).uppercase(),
                    color = ZashiColors.Text.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Name + balance ticker
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = token.name,
                color = ZashiColors.Text.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (balancesAvailable) {
                    "${token.balance} ${token.ticker}"
                } else {
                    "$HIDE_BALANCE_PLACEHOLDER ${token.ticker}"
                },
                color = ZashiColors.Text.textTertiary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // USD value
        if (token.usdValue != null) {
            Text(
                text = if (balancesAvailable) token.usdValue else HIDE_BALANCE_PLACEHOLDER,
                color = ZashiColors.Text.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                textAlign = TextAlign.End,
            )
        }
    }
}
