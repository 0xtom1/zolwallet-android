package co.electriccoin.zcash.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.common.appbar.ZashiMainTopAppBarState
import co.electriccoin.zcash.ui.common.appbar.ZashiTopAppBarWithAccountSelection
import co.electriccoin.zcash.ui.design.component.BigIconButtonState
import co.electriccoin.zcash.ui.design.component.BlankBgScaffold
import co.electriccoin.zcash.ui.design.newcomponent.PreviewScreens
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors
import co.electriccoin.zcash.ui.design.theme.dimensions.ZashiDimensions
import co.electriccoin.zcash.ui.design.util.getValue
import co.electriccoin.zcash.ui.design.util.stringRes
import co.electriccoin.zcash.ui.fixture.ZashiMainTopAppBarStateFixture
import co.electriccoin.zcash.ui.screen.home.bottomnav.BottomNavTab
import co.electriccoin.zcash.ui.screen.home.bottomnav.ZolBottomNavBar
import co.electriccoin.zcash.ui.screen.home.bottomnav.ZolBottomNavBarState
import co.electriccoin.zcash.ui.screen.home.error.WalletErrorMessageState
import co.electriccoin.zcash.ui.screen.home.tokenlist.TokenListState
import co.electriccoin.zcash.ui.screen.home.tokenlist.TokenRowState
import co.electriccoin.zcash.ui.screen.home.tokenlist.tokenListItems

@Composable
internal fun HomeView(
    appBarState: ZashiMainTopAppBarState?,
    state: HomeState,
    tokenListState: TokenListState,
    bottomNavState: ZolBottomNavBarState,
) {
    BlankBgScaffold(
        topBar = { ZashiTopAppBarWithAccountSelection(appBarState) },
        bottomBar = { ZolBottomNavBar(bottomNavState) }
    ) { paddingValues ->
        Content(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding() + ZashiDimensions.Spacing.spacingLg),
            paddingValues = paddingValues,
            state = state,
            tokenListState = tokenListState,
        )
    }
}

@Composable
private fun Content(
    paddingValues: PaddingValues,
    state: HomeState,
    tokenListState: TokenListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())
    ) {
        // Portfolio header (total USD)
        item(key = "portfolio_header") {
            PortfolioHeader(
                totalUsd = tokenListState.totalPortfolioUsd,
                modifier = Modifier.padding(
                    start = ZcashTheme.dimens.screenHorizontalSpacingRegular,
                    end = ZcashTheme.dimens.screenHorizontalSpacingRegular,
                )
            )
        }

        // Nav buttons (Receive, Send)
        item(key = "nav_buttons") {
            Spacer(Modifier.height(12.dp))
            NavButtons(state = state)
            Spacer(Modifier.height(16.dp))
        }

        // Home message (syncing, restoring, backup, etc.)
        item(key = "home_message") {
            HomeMessage(
                state = state.message,
                modifier = Modifier.padding(
                    start = ZcashTheme.dimens.screenHorizontalSpacingRegular,
                    end = ZcashTheme.dimens.screenHorizontalSpacingRegular,
                )
            )
        }

        item(key = "token_list_spacer") {
            Spacer(Modifier.height(8.dp))
        }

        // Token list
        tokenListItems(state = tokenListState)
    }
}

@Composable
private fun PortfolioHeader(
    totalUsd: String?,
    modifier: Modifier = Modifier,
) {
    if (totalUsd != null) {
        Text(
            text = totalUsd,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            style = ZcashTheme.extendedTypography.balanceWidgetStyles.first,
            color = ZashiColors.Text.textPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NavButtons(
    state: HomeState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ZcashTheme.dimens.screenHorizontalSpacingRegular),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
    ) {
        CompactNavButton(
            icon = state.firstButton.icon,
            label = state.firstButton.text.getValue(),
            onClick = state.firstButton.onClick,
            modifier = Modifier.testTag(HomeTags.RECEIVE),
        )
        CompactNavButton(
            icon = state.secondButton.icon,
            label = state.secondButton.text.getValue(),
            onClick = state.secondButton.onClick,
            modifier = Modifier.testTag(HomeTags.SEND),
        )
    }
}

@Composable
private fun CompactNavButton(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ZashiColors.Surfaces.bgSecondary)
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = ZashiColors.Text.textPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = ZashiColors.Text.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@PreviewScreens
@Composable
private fun Preview() {
    ZcashTheme {
        HomeView(
            appBarState = ZashiMainTopAppBarStateFixture.new(),
            state =
                HomeState(
                    firstButton =
                        BigIconButtonState(
                            text = stringRes("Receive"),
                            icon = R.drawable.ic_home_receive,
                            onClick = {}
                        ),
                    secondButton =
                        BigIconButtonState(
                            text = stringRes("Send"),
                            icon = R.drawable.ic_home_send,
                            onClick = {}
                        ),
                    message = WalletErrorMessageState(onClick = {})
                ),
            tokenListState =
                TokenListState(
                    tokens = listOf(
                        TokenRowState(
                            id = "ZEC",
                            iconRes = R.drawable.ic_token_zec,
                            iconUrl = null,
                            name = "Zcash",
                            ticker = "ZEC",
                            balance = "1.5",
                            usdValue = "$45.00",
                        ),
                        TokenRowState(
                            id = "SOL",
                            iconRes = R.drawable.ic_token_sol,
                            iconUrl = null,
                            name = "Solana",
                            ticker = "SOL",
                            balance = "2.0",
                            usdValue = "$300.00",
                        ),
                    ),
                    totalPortfolioUsd = "$345.00",
                    isLoading = false,
                ),
            bottomNavState =
                ZolBottomNavBarState(
                    selectedTab = BottomNavTab.PORTFOLIO,
                    onPortfolioClick = {},
                    onWalletsClick = {},
                    onBridgeClick = {},
                    onSwapClick = {},
                    onPayClick = {},
                ),
        )
    }
}
