package xyz.zolapp.market.provider

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import xyz.zolapp.market.model.TokenPrice
import xyz.zolapp.market.network.MarketHttpClientFactory

/**
 * Fetches token prices from Helius DAS API via the zol-rpc-proxy.
 * All calls go through [MarketHttpClientFactory] which is Tor-routed when the user has Tor enabled.
 */
class HeliusPriceProvider(
    private val httpClientFactory: MarketHttpClientFactory,
    private val rpcUrl: String = RPC_URL,
) {
    /**
     * Fetch price for a single token by mint address.
     * Uses Helius `getAsset` — returns null if the token has no price data.
     */
    suspend fun getPrice(mint: String): TokenPrice? {
        val response =
            httpClientFactory.create().post(rpcUrl) {
                contentType(ContentType.Application.Json)
                setBody("""{"jsonrpc":"2.0","id":1,"method":"getAsset","params":{"id":"$mint"}}""")
            }.body<JsonObject>()

        val result = response["result"]?.jsonObject ?: return null
        return parseTokenPrice(mint, result)
    }

    /**
     * Fetch prices for multiple tokens in a single batch call.
     * Uses Helius `getAssetBatch` — tokens with no price data are omitted from the result.
     */
    suspend fun getPrices(mints: List<String>): Map<String, TokenPrice> {
        if (mints.isEmpty()) return emptyMap()

        val idsJson = mints.joinToString(",") { "\"$it\"" }
        val response =
            httpClientFactory.create().post(rpcUrl) {
                contentType(ContentType.Application.Json)
                setBody("""{"jsonrpc":"2.0","id":1,"method":"getAssetBatch","params":{"ids":[$idsJson]}}""")
            }.body<JsonObject>()

        val results = response["result"]?.jsonArray ?: return emptyMap()
        val prices = mutableMapOf<String, TokenPrice>()

        for (item in results) {
            val obj = item.jsonObject
            val id = obj["id"]?.jsonPrimitive?.content ?: continue
            val price = parseTokenPrice(id, obj) ?: continue
            prices[id] = price
        }

        return prices
    }

    private fun parseTokenPrice(
        mint: String,
        asset: JsonObject,
    ): TokenPrice? {
        val tokenInfo = asset["token_info"]?.jsonObject ?: return null
        val priceInfo = tokenInfo["price_info"]?.jsonObject ?: return null
        val pricePerToken = priceInfo["price_per_token"]?.jsonPrimitive?.doubleOrNull ?: return null

        return TokenPrice(
            mint = mint,
            priceUsd = pricePerToken,
            timestampMs = System.currentTimeMillis(),
        )
    }

    companion object {
        const val RPC_URL = "https://wallet-api-5arl64l7ia-uc.a.run.app/helius"
    }
}
