package xyz.zolapp.solana.rpc

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import org.sol4k.PublicKey
import org.sol4k.Transaction
import xyz.zolapp.solana.model.SplTokenInfo
import java.util.Base64

/**
 * Makes Solana JSON-RPC calls to the Zol RPC proxy via an injectable [SolanaHttpClientFactory].
 * The factory is provided by the UI layer and may return a Tor-routed client when Tor is enabled.
 * Sol4k is still used for key derivation, address types, and transaction signing.
 */
class SolanaRpcProvider(
    private val httpClientFactory: SolanaHttpClientFactory,
    private val rpcUrl: String = MAINNET_RPC_URL,
) {
    suspend fun getBalance(publicKey: PublicKey): Long {
        val response = httpClientFactory.create().post(rpcUrl) {
            contentType(ContentType.Application.Json)
            setBody("""{"jsonrpc":"2.0","id":1,"method":"getBalance","params":["${publicKey.toBase58()}"]}""")
        }.body<JsonObject>()
        return response["result"]!!.jsonObject["value"]!!.jsonPrimitive.long
    }

    suspend fun getLatestBlockhash(): String {
        val response = httpClientFactory.create().post(rpcUrl) {
            contentType(ContentType.Application.Json)
            setBody("""{"jsonrpc":"2.0","id":1,"method":"getLatestBlockhash","params":[]}""")
        }.body<JsonObject>()
        return response["result"]!!.jsonObject["value"]!!.jsonObject["blockhash"]!!.jsonPrimitive.content
    }

    suspend fun sendTransaction(transaction: Transaction): String {
        val serialized = Base64.getEncoder().encodeToString(transaction.serialize())
        val response = httpClientFactory.create().post(rpcUrl) {
            contentType(ContentType.Application.Json)
            setBody("""{"jsonrpc":"2.0","id":1,"method":"sendTransaction","params":["$serialized",{"encoding":"base64"}]}""")
        }.body<JsonObject>()
        return response["result"]!!.jsonPrimitive.content
    }

    @Suppress("NestedBlockDepth", "CyclomaticComplexMethod")
    suspend fun getAssetsByOwner(publicKey: PublicKey): Pair<List<SplTokenInfo>, SplTokenInfo?> {
        val response = httpClientFactory.create().post(rpcUrl) {
            contentType(ContentType.Application.Json)
            setBody(
                """{"jsonrpc":"2.0","id":1,"method":"getAssetsByOwner","params":{""" +
                    """"ownerAddress":"${publicKey.toBase58()}",""" +
                    """"displayOptions":{"showFungible":true,"showNativeBalance":true}}}"""
            )
        }.body<JsonObject>()

        val result = response["result"]!!.jsonObject
        val items = result["items"]?.jsonArray ?: return Pair(emptyList(), null)

        val tokens = mutableListOf<SplTokenInfo>()
        for (item in items) {
            val obj = item.jsonObject
            val iface = obj["interface"]?.jsonPrimitive?.content ?: continue
            if (iface != "FungibleToken" && iface != "FungibleAsset") continue

            val id = obj["id"]?.jsonPrimitive?.content ?: continue
            val content = obj["content"]?.jsonObject ?: continue
            val metadata = content["metadata"]?.jsonObject
            val name = metadata?.get("name")?.jsonPrimitive?.content ?: ""
            val symbol = metadata?.get("symbol")?.jsonPrimitive?.content ?: ""

            val links = content["links"]?.jsonObject
            val imageUrl = links?.get("image")?.jsonPrimitive?.content

            val tokenInfo = obj["token_info"]?.jsonObject
            val balance = tokenInfo?.get("balance")?.jsonPrimitive?.longOrNull ?: 0L
            val decimals = tokenInfo?.get("decimals")?.jsonPrimitive?.int ?: 0
            val priceInfo = tokenInfo?.get("price_info")?.jsonObject
            val pricePerToken = priceInfo?.get("price_per_token")?.jsonPrimitive?.doubleOrNull
            val totalPrice = priceInfo?.get("total_price")?.jsonPrimitive?.doubleOrNull

            val uiAmount = if (decimals > 0) balance.toDouble() / Math.pow(10.0, decimals.toDouble()) else balance.toDouble()

            tokens.add(
                SplTokenInfo(
                    mint = id,
                    name = name,
                    symbol = symbol,
                    imageUrl = imageUrl,
                    balance = balance,
                    decimals = decimals,
                    uiAmount = uiAmount,
                    pricePerToken = pricePerToken,
                    valueUsd = totalPrice
                )
            )
        }

        // Parse native SOL balance from nativeBalance field
        val nativeBalance = result["nativeBalance"]?.jsonObject
        val solToken = nativeBalance?.let { nb ->
            val lamports = nb["lamports"]?.jsonPrimitive?.longOrNull ?: 0L
            val solAmount = nb["sol_balance"]?.jsonPrimitive?.doubleOrNull ?: (lamports / 1_000_000_000.0)
            val solPrice = nb["price_per_sol"]?.jsonPrimitive?.doubleOrNull
            val solTotal = nb["total_price"]?.jsonPrimitive?.doubleOrNull
            SplTokenInfo(
                mint = "",
                name = "Solana",
                symbol = "SOL",
                imageUrl = null,
                balance = lamports,
                decimals = SOL_DECIMALS,
                uiAmount = solAmount,
                pricePerToken = solPrice,
                valueUsd = solTotal
            )
        }

        return Pair(tokens, solToken)
    }

    fun isValidAddress(address: String): Boolean =
        try {
            PublicKey(address).toBase58() == address
        } catch (_: Exception) {
            false
        }

    companion object {
        const val MAINNET_RPC_URL = "https://wallet-api-5arl64l7ia-uc.a.run.app/helius"
        const val DEVNET_RPC_URL = "https://api.devnet.solana.com"
        private const val SOL_DECIMALS = 9
    }
}
