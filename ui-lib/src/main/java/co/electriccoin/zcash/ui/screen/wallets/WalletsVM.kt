package co.electriccoin.zcash.ui.screen.wallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.sdk.ANDROID_STATE_FLOW_TIMEOUT
import co.electriccoin.zcash.ui.NavigationRouter
import co.electriccoin.zcash.ui.common.datasource.SolanaWalletDataSource
import co.electriccoin.zcash.ui.common.model.SolanaWalletConfig
import co.electriccoin.zcash.ui.common.repository.EphemeralAddressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.zolapp.solana.repository.SolanaRepository

class WalletsVM(
    private val walletDataSource: SolanaWalletDataSource,
    private val solanaRepository: SolanaRepository,
    private val navigationRouter: NavigationRouter,
    private val ephemeralAddressRepository: EphemeralAddressRepository,
) : ViewModel() {
    val state: StateFlow<WalletsState> =
        combine(
            walletDataSource.config,
            ephemeralAddressRepository.observeAll(),
        ) { config, ephemeralAddresses ->
            createState(config, ephemeralAddresses.map { addr ->
                EphemeralAddressItemState(
                    address = addr.address,
                    addressShort = shortenAddress(addr.address),
                )
            })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
            initialValue = WalletsState(
                wallets = emptyList(),
                canCreateMore = true,
                isLoading = true,
                onCreate = ::onCreateWallet,
                onBack = ::onBack,
                ephemeralAddresses = emptyList(),
                onCreateEphemeral = ::onCreateEphemeral,
            )
        )

    private suspend fun createState(
        config: SolanaWalletConfig,
        ephemeralItems: List<EphemeralAddressItemState>
    ): WalletsState {
        val items = config.wallets.map { entry ->
            val address = solanaRepository.getAddress(entry.accountIndex)
            WalletItemState(
                accountIndex = entry.accountIndex,
                name = entry.name,
                address = address,
                addressShort = shortenAddress(address),
                isSelected = entry.accountIndex == config.selectedAccountIndex,
                onSelect = { onSelectWallet(entry.accountIndex) },
                onRename = { newName -> onRenameWallet(entry.accountIndex, newName) },
            )
        }
        return WalletsState(
            wallets = items,
            canCreateMore = config.wallets.size < SolanaWalletConfig.MAX_WALLETS,
            isLoading = false,
            onCreate = ::onCreateWallet,
            onBack = ::onBack,
            ephemeralAddresses = ephemeralItems,
            onCreateEphemeral = ::onCreateEphemeral,
        )
    }

    private fun onCreateWallet(name: String) {
        viewModelScope.launch {
            walletDataSource.createWallet(name)
        }
    }

    private fun onRenameWallet(accountIndex: Int, newName: String) {
        viewModelScope.launch {
            walletDataSource.renameWallet(accountIndex, newName)
        }
    }

    private fun onSelectWallet(accountIndex: Int) {
        viewModelScope.launch {
            walletDataSource.selectWallet(accountIndex)
            solanaRepository.fetchTokenData(accountIndex)
        }
    }

    private fun onCreateEphemeral() {
        viewModelScope.launch {
            ephemeralAddressRepository.create()
        }
    }

    private fun onBack() = navigationRouter.back()

    fun nextDefaultName(): String = walletDataSource.nextDefaultName()

    companion object {
        private fun shortenAddress(address: String): String =
            if (address.length > 8) {
                "${address.take(4)}...${address.takeLast(4)}"
            } else {
                address
            }
    }
}
