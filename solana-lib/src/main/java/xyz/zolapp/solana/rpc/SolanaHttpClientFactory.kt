package xyz.zolapp.solana.rpc

import io.ktor.client.HttpClient

interface SolanaHttpClientFactory {
    suspend fun create(): HttpClient
}
