package xyz.zolapp.solana.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import xyz.zolapp.solana.crypto.SolanaKeypairProvider
import xyz.zolapp.solana.crypto.SolanaKeypairProviderImpl
import xyz.zolapp.solana.datasource.SolanaDataSource
import xyz.zolapp.solana.repository.SolanaRepository
import xyz.zolapp.solana.rpc.SolanaHttpClientFactory
import xyz.zolapp.solana.rpc.SolanaRpcProvider
import xyz.zolapp.solana.usecase.GetSolanaAddressUseCase
import xyz.zolapp.solana.usecase.GetSolanaBalanceUseCase
import xyz.zolapp.solana.usecase.SendSolUseCase
import xyz.zolapp.solana.usecase.ValidateSolanaAddressUseCase

fun solanaModule(isTestnet: Boolean) =
    module {
        single { SolanaRpcProvider(get<SolanaHttpClientFactory>()) }
        singleOf(::SolanaDataSource)
        singleOf(::SolanaRepository)
        factoryOf(::GetSolanaBalanceUseCase)
        factoryOf(::GetSolanaAddressUseCase)
        factoryOf(::SendSolUseCase)
        factoryOf(::ValidateSolanaAddressUseCase)
    }
