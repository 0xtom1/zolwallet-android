package co.electriccoin.zcash.ui.screen.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.sdk.ANDROID_STATE_FLOW_TIMEOUT
import co.electriccoin.zcash.ui.NavigationRouter
import co.electriccoin.zcash.ui.NavigationTargets
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.common.datasource.SolanaWalletDataSource
import co.electriccoin.zcash.ui.common.model.KeystoneAccount
import co.electriccoin.zcash.ui.common.model.WalletAccount
import co.electriccoin.zcash.ui.common.model.ZashiAccount
import co.electriccoin.zcash.ui.common.repository.EphemeralAddressRepository
import co.electriccoin.zcash.ui.common.usecase.CopyToClipboardUseCase
import co.electriccoin.zcash.ui.common.usecase.ObserveSelectedWalletAccountUseCase
import co.electriccoin.zcash.ui.design.component.IconButtonState
import co.electriccoin.zcash.ui.design.util.stringRes
import co.electriccoin.zcash.ui.design.util.stringResByAddress
import co.electriccoin.zcash.ui.screen.receive.ReceiveAddressState.ColorMode.DEFAULT
import co.electriccoin.zcash.ui.screen.receive.ReceiveAddressState.ColorMode.KEYSTONE
import co.electriccoin.zcash.ui.screen.receive.ReceiveAddressState.ColorMode.ZASHI
import co.electriccoin.zcash.ui.screen.receive.ReceiveAddressType.Sapling
import co.electriccoin.zcash.ui.screen.receive.ReceiveAddressType.Transparent
import co.electriccoin.zcash.ui.screen.receive.ReceiveAddressType.Unified
import co.electriccoin.zcash.ui.screen.receive.info.ShieldedAddressInfoArgs
import co.electriccoin.zcash.ui.screen.receive.info.TransparentAddressInfoArgs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.zolapp.solana.repository.SolanaRepository
import java.net.URLEncoder

class ReceiveVM(
    observeSelectedWalletAccount: ObserveSelectedWalletAccountUseCase,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val navigationRouter: NavigationRouter,
    private val solanaRepository: SolanaRepository,
    private val ephemeralAddressRepository: EphemeralAddressRepository,
    private val solanaWalletDataSource: SolanaWalletDataSource,
) : ViewModel() {
    private val expandedIndex = MutableStateFlow(0)
    private val solanaAddress = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            solanaAddress.value = solanaRepository.getAddress(solanaWalletDataSource.selectedAccountIndex.value)
        }
        // Auto-generate first ephemeral address if none exists
        viewModelScope.launch {
            if (ephemeralAddressRepository.get() == null) {
                ephemeralAddressRepository.create()
            }
        }
    }

    internal val state =
        combine(
            expandedIndex,
            observeSelectedWalletAccount.require(),
            solanaAddress,
            ephemeralAddressRepository.observe(),
        ) { expandedIndex, account, solAddress, ephemeralAddress ->
            ReceiveState(
                items =
                    listOfNotNull(
                        createAddressState(
                            account = account,
                            address = account.unified.address.address,
                            type = Unified,
                            isExpanded = expandedIndex == 0,
                            onClick = { onAddressClick(0) }
                        ),
                        createAddressState(
                            account = account,
                            address = account.transparent.address.address,
                            type = Transparent,
                            isExpanded = expandedIndex == 1,
                            onClick = { onAddressClick(1) }
                        ),
                        solAddress?.let {
                            createSolanaAddressState(
                                address = it,
                                isExpanded = expandedIndex == 2,
                                onClick = { onAddressClick(2) }
                            )
                        },
                        ephemeralAddress?.let {
                            createEphemeralAddressState(
                                address = it.address,
                                isExpanded = expandedIndex == 3,
                                onClick = { onAddressClick(3) }
                            )
                        },
                    ),
                isLoading = false,
                onBack = ::onBack
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
            initialValue =
                ReceiveState(
                    items = null,
                    isLoading = true,
                    onBack = ::onBack
                )
        )

    private fun onBack() = navigationRouter.back()

    @Suppress("CyclomaticComplexMethod")
    private fun createAddressState(
        account: WalletAccount,
        address: String,
        type: ReceiveAddressType,
        isExpanded: Boolean,
        onClick: () -> Unit,
    ) = ReceiveAddressState(
        icon =
            when (account) {
                is KeystoneAccount -> co.electriccoin.zcash.ui.design.R.drawable.ic_item_keystone
                is ZashiAccount -> R.drawable.ic_zec_round_full
            },
        title =
            when (account) {
                is KeystoneAccount ->
                    if (type == Unified) {
                        stringRes(R.string.receive_wallet_address_shielded_keystone)
                    } else {
                        stringRes(R.string.receive_wallet_address_transparent_keystone)
                    }

                is ZashiAccount ->
                    if (type == Unified) {
                        stringRes(R.string.receive_wallet_address_shielded)
                    } else {
                        stringRes(R.string.receive_wallet_address_transparent)
                    }
            },
        subtitle = stringResByAddress(value = address, middle = true),
        isShielded = type == Unified,
        showCopy = type == Unified,
        showRequest = true,
        onCopyClicked = {
            copyToClipboard(
                value = address
            )
        },
        onQrClicked = { onQrCodeClick(type) },
        onRequestClicked = { onRequestClick(type) },
        onClick = onClick,
        isExpanded = isExpanded,
        colorMode =
            when (account) {
                is KeystoneAccount -> if (type == Unified) KEYSTONE else DEFAULT
                is ZashiAccount -> if (type == Unified) ZASHI else DEFAULT
            },
        infoIconButton =
            IconButtonState(
                when (type) {
                    Sapling,
                    Unified ->
                        when (account) {
                            is KeystoneAccount -> R.drawable.ic_receive_ks_shielded_info
                            is ZashiAccount -> R.drawable.ic_receive_zashi_shielded_info
                        }
                    Transparent -> R.drawable.ic_receive_zcash_info
                },
                onClick = { onAddressInfoClick(type) }
            )
    )

    private fun createSolanaAddressState(
        address: String,
        isExpanded: Boolean,
        onClick: () -> Unit,
    ) = ReceiveAddressState(
        icon = R.drawable.ic_token_sol,
        title = stringRes("Solana Address"),
        subtitle = stringResByAddress(value = address, middle = true),
        isShielded = false,
        showCopy = true,
        showRequest = false,
        onCopyClicked = { copyToClipboard(value = address) },
        onQrClicked = { onSolanaQrCodeClick(address) },
        onRequestClicked = {},
        onClick = onClick,
        isExpanded = isExpanded,
        colorMode = DEFAULT,
        infoIconButton = null,
        onGenerateNewClicked = {
            viewModelScope.launch {
                val name = solanaWalletDataSource.nextDefaultName()
                solanaWalletDataSource.createWallet(name)
                // Select the newly created wallet (it will be the last one)
                val config = solanaWalletDataSource.config.value
                val newWallet = config.wallets.lastOrNull()
                if (newWallet != null) {
                    solanaWalletDataSource.selectWallet(newWallet.accountIndex)
                    solanaAddress.value = solanaRepository.getAddress(newWallet.accountIndex)
                }
            }
        },
    )

    private fun createEphemeralAddressState(
        address: String,
        isExpanded: Boolean,
        onClick: () -> Unit,
    ): ReceiveAddressState {
        @Suppress("ktlint:standard:max-line-length")
        val encodedAddress = URLEncoder.encode(address, "UTF-8")
        return ReceiveAddressState(
            icon = R.drawable.ic_zec_round_full,
            title = stringRes(R.string.receive_wallet_address_ephemeral),
            subtitle = stringResByAddress(value = address, middle = true),
            isShielded = false,
            showCopy = true,
            showRequest = true,
            onCopyClicked = { copyToClipboard(value = address) },
            onQrClicked = {
                navigationRouter.forward(
                    "${NavigationTargets.QR_CODE}/${Transparent.ordinal}?address=$encodedAddress"
                )
            },
            onRequestClicked = {
                navigationRouter.forward(
                    "${NavigationTargets.REQUEST}/${Transparent.ordinal}?address=$encodedAddress"
                )
            },
            onClick = onClick,
            isExpanded = isExpanded,
            colorMode = DEFAULT,
            infoIconButton = null,
            onGenerateNewClicked = {
                viewModelScope.launch {
                    ephemeralAddressRepository.create()
                }
            },
        )
    }

    private fun onSolanaQrCodeClick(address: String) {
        navigationRouter.forward(
            co.electriccoin.zcash.ui.screen.solanareceive.SolanaReceiveArgs
        )
    }

    private fun onRequestClick(addressType: ReceiveAddressType) =
        navigationRouter.forward("${NavigationTargets.REQUEST}/${addressType.ordinal}")

    private fun onQrCodeClick(addressType: ReceiveAddressType) =
        navigationRouter.forward("${NavigationTargets.QR_CODE}/${addressType.ordinal}")

    private fun onAddressClick(index: Int) {
        expandedIndex.update { index }
    }

    private fun onAddressInfoClick(type: ReceiveAddressType) {
        when (type) {
            Sapling,
            Unified -> navigationRouter.forward(ShieldedAddressInfoArgs)
            Transparent -> navigationRouter.forward(TransparentAddressInfoArgs)
        }
    }
}
