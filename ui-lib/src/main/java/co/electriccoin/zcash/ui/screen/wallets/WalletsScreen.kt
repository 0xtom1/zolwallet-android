package co.electriccoin.zcash.ui.screen.wallets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.ui.design.component.BlankBgScaffold
import co.electriccoin.zcash.ui.design.component.ZashiSmallTopAppBar
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors
import co.electriccoin.zcash.ui.screen.home.bottomnav.BottomNavTab
import co.electriccoin.zcash.ui.screen.home.bottomnav.ZolBottomNavBarForTab
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object WalletsArgs

@Composable
fun WalletsScreen(viewModel: WalletsVM = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<WalletItemState?>(null) }

    BlankBgScaffold(
        topBar = {
            ZashiSmallTopAppBar(title = "Wallets")
        },
        bottomBar = { ZolBottomNavBarForTab(BottomNavTab.WALLETS) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            items(
                items = state.wallets,
                key = { it.accountIndex },
            ) { wallet ->
                WalletRow(
                    wallet = wallet,
                    onRenameClick = { renameTarget = wallet },
                )
                HorizontalDivider(color = ZashiColors.Surfaces.bgSecondary)
            }

            if (state.canCreateMore) {
                item {
                    CreateWalletRow(onClick = { showCreateDialog = true })
                }
            }
        }
    }

    if (showCreateDialog) {
        WalletNameDialog(
            title = "Create Wallet",
            initialName = viewModel.nextDefaultName(),
            onConfirm = { name ->
                state.onCreate(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }

    renameTarget?.let { wallet ->
        WalletNameDialog(
            title = "Rename Wallet",
            initialName = wallet.name,
            onConfirm = { newName ->
                wallet.onRename(newName)
                renameTarget = null
            },
            onDismiss = { renameTarget = null },
        )
    }
}

@Composable
private fun WalletRow(
    wallet: WalletItemState,
    onRenameClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { wallet.onSelect() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (wallet.isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = ZashiColors.Text.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Spacer(modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = wallet.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ZashiColors.Text.textPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = wallet.addressShort,
                fontSize = 13.sp,
                color = ZashiColors.Text.textTertiary,
            )
        }

        IconButton(onClick = onRenameClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Rename",
                tint = ZashiColors.Text.textTertiary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun CreateWalletRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Create wallet",
            tint = ZashiColors.Text.textTertiary,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Create Wallet",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = ZashiColors.Text.textTertiary,
        )
    }
}

@Composable
private fun WalletNameDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Wallet name") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
