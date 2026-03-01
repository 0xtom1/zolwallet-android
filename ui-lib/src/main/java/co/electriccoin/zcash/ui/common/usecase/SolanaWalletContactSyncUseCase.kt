package co.electriccoin.zcash.ui.common.usecase

import co.electriccoin.zcash.spackle.Twig
import co.electriccoin.zcash.ui.common.datasource.SolanaWalletDataSource
import co.electriccoin.zcash.ui.common.model.SolanaWalletEntry
import co.electriccoin.zcash.ui.common.repository.AddressBookRepository
import co.electriccoin.zcash.ui.common.repository.EnhancedABContact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import xyz.zolapp.solana.repository.SolanaRepository

class SolanaWalletContactSyncUseCase(
    private val walletDataSource: SolanaWalletDataSource,
    private val addressBookRepository: AddressBookRepository,
    private val solanaRepository: SolanaRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private var isSyncing = false

    // Cached previous state for change detection
    private var prevWalletNames = mapOf<String, String>() // address -> walletName
    private var prevContactNames = mapOf<String, String>() // address -> contactName

    // Addresses where user deleted the auto-created contact
    private val suppressedAddresses = mutableSetOf<String>()

    init {
        scope.launch {
            combine(
                walletDataSource.config,
                addressBookRepository.contacts.filterNotNull(),
            ) { config, contacts ->
                config to contacts
            }.collect { (config, contacts) ->
                mutex.withLock {
                    if (isSyncing) return@withLock
                    isSyncing = true
                    try {
                        performSync(config.wallets, contacts)
                    } catch (e: Exception) {
                        Twig.error(e) { "Wallet-contact sync failed" }
                    } finally {
                        isSyncing = false
                    }
                }
            }
        }
    }

    private suspend fun performSync(
        wallets: List<SolanaWalletEntry>,
        allContacts: List<EnhancedABContact>,
    ) {
        // Build address -> wallet map
        val walletsByAddress = mutableMapOf<String, SolanaWalletEntry>()
        for (wallet in wallets) {
            val address = solanaRepository.getAddress(wallet.accountIndex)
            walletsByAddress[address] = wallet
        }

        // SOL contacts indexed by address
        val solContacts = allContacts.filter { it.contact.chain?.lowercase() == "sol" }
        val solContactsByAddress = solContacts.associateBy { it.address }

        // Sync each wallet
        for ((address, wallet) in walletsByAddress) {
            val contact = solContactsByAddress[address]

            if (contact == null) {
                handleMissingContact(address, wallet)
            } else if (wallet.name != contact.name) {
                handleNameMismatch(address, wallet, contact)
            }
        }

        // Detect user-deleted contacts
        for (address in prevContactNames.keys) {
            if (walletsByAddress.containsKey(address) && !solContactsByAddress.containsKey(address)) {
                suppressedAddresses.add(address)
            }
        }

        // Update caches
        prevWalletNames = walletsByAddress.mapValues { it.value.name }
        prevContactNames = solContactsByAddress
            .filter { walletsByAddress.containsKey(it.key) }
            .mapValues { it.value.name }
    }

    private fun handleMissingContact(address: String, wallet: SolanaWalletEntry) {
        if (address in suppressedAddresses) {
            // Re-create only if wallet was renamed since suppression
            val prevName = prevWalletNames[address]
            if (prevName != null && prevName != wallet.name) {
                suppressedAddresses.remove(address)
                addressBookRepository.saveContact(name = wallet.name, address = address, chain = SOL_CHAIN)
            }
        } else {
            addressBookRepository.saveContact(name = wallet.name, address = address, chain = SOL_CHAIN)
        }
    }

    private fun handleNameMismatch(
        address: String,
        wallet: SolanaWalletEntry,
        contact: EnhancedABContact,
    ) {
        val prevWalletName = prevWalletNames[address]
        val prevContactName = prevContactNames[address]

        val walletRenamed = prevWalletName != null && prevWalletName != wallet.name
        val contactRenamed = prevContactName != null && prevContactName != contact.name

        when {
            walletRenamed && !contactRenamed -> {
                // Wallet side changed -> push to contact
                addressBookRepository.updateContact(
                    contact = contact,
                    name = wallet.name,
                    address = address,
                    chain = SOL_CHAIN,
                )
            }
            contactRenamed && !walletRenamed -> {
                // Contact side changed -> push to wallet
                scope.launch {
                    walletDataSource.renameWallet(wallet.accountIndex, contact.name)
                }
            }
            else -> {
                // Both changed or first-run mismatch -> wallet wins
                addressBookRepository.updateContact(
                    contact = contact,
                    name = wallet.name,
                    address = address,
                    chain = SOL_CHAIN,
                )
            }
        }
    }

    companion object {
        private const val SOL_CHAIN = "SOL"
    }
}
