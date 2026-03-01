package co.electriccoin.zcash.ui.screen.comingsoon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.design.component.BlankBgScaffold
import co.electriccoin.zcash.ui.design.component.ZashiSmallTopAppBar
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors
import co.electriccoin.zcash.ui.screen.home.bottomnav.BottomNavTab
import co.electriccoin.zcash.ui.screen.home.bottomnav.ZolBottomNavBarForTab
import kotlinx.serialization.Serializable

@Serializable
data object ComingSoonArgs

@Composable
fun ComingSoonScreen() {
    BlankBgScaffold(
        topBar = {
            ZashiSmallTopAppBar(title = "Swap")
        },
        bottomBar = { ZolBottomNavBarForTab(BottomNavTab.SWAP) }
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Coming Soon",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = ZashiColors.Text.textPrimary,
            )
        }
    }
}

@Composable
fun ComingSoonContent() {
    BlankBgScaffold(
        topBar = {
            ZashiSmallTopAppBar(title = "Swap")
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Coming Soon",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = ZashiColors.Text.textPrimary,
            )
        }
    }
}
