package co.electriccoin.zcash.ui.screen.wallets

import androidx.compose.runtime.Immutable

@Immutable
data class WalletsState(
    val wallets: List<WalletItemState>,
    val canCreateMore: Boolean,
    val isLoading: Boolean,
    val onCreate: (name: String) -> Unit,
    val onBack: () -> Unit,
    val ephemeralAddresses: List<EphemeralAddressItemState> = emptyList(),
    val onCreateEphemeral: () -> Unit = {},
    val onCreateEphemeralNamed: (name: String) -> Unit = {},
)

@Immutable
data class WalletItemState(
    val accountIndex: Int,
    val name: String,
    val address: String,
    val addressShort: String,
    val isSelected: Boolean,
    val onSelect: () -> Unit,
    val onRename: (newName: String) -> Unit,
)

@Immutable
data class EphemeralAddressItemState(
    val address: String,
    val addressShort: String,
    val name: String,
    val onRename: (newName: String) -> Unit,
)
