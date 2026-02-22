package xyz.zolapp.market.di

import org.koin.dsl.module
import xyz.zolapp.market.cache.PriceCache
import xyz.zolapp.market.network.MarketHttpClientFactory
import xyz.zolapp.market.provider.HeliusPriceProvider
import xyz.zolapp.market.repository.PriceRepository

val marketModule =
    module {
        single { PriceCache() }
        single { HeliusPriceProvider(get<MarketHttpClientFactory>()) }
        single { PriceRepository(get(), get()) }
    }
