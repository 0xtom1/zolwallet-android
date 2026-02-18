package co.electriccoin.zcash.di

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import co.electriccoin.zcash.ui.common.provider.HttpClientProvider
import co.electriccoin.zcash.ui.common.provider.PersistableWalletProvider
import co.electriccoin.zcash.ui.screen.solanabalance.SolanaBalanceWidgetVM
import co.electriccoin.zcash.ui.screen.solanareceive.SolanaReceiveVM
import co.electriccoin.zcash.ui.screen.solanasend.SolanaSendViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import xyz.zolapp.solana.crypto.SolanaKeypairProvider
import xyz.zolapp.solana.crypto.SolanaKeypairProviderImpl
import xyz.zolapp.solana.rpc.SolanaHttpClientFactory

val solanaViewModelModule =
    module {
        // Bridge: HttpClientProvider → SolanaHttpClientFactory (Tor-aware when enabled)
        single<SolanaHttpClientFactory> {
            val httpClientProvider = get<HttpClientProvider>()
            object : SolanaHttpClientFactory {
                override suspend fun create() = httpClientProvider.create()
            }
        }

        // Bridge: connect BIP39 seed from Zcash storage to Solana keypair provider
        single {
            SolanaKeypairProviderImpl(
                seedProvider = {
                    val wallet = get<PersistableWalletProvider>().requirePersistableWallet()
                    Mnemonics.MnemonicCode(wallet.seedPhrase.joinToString()).toSeed()
                }
            )
        } bind SolanaKeypairProvider::class

        viewModelOf(::SolanaSendViewModel)
        viewModelOf(::SolanaReceiveVM)
        viewModelOf(::SolanaBalanceWidgetVM)
    }
