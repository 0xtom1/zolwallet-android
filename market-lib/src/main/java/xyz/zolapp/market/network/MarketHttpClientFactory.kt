package xyz.zolapp.market.network

import io.ktor.client.HttpClient

interface MarketHttpClientFactory {
    suspend fun create(): HttpClient
}
