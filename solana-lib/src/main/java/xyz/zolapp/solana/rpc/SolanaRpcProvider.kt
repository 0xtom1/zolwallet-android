package xyz.zolapp.solana.rpc

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.sol4k.PublicKey
import org.sol4k.Transaction
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

    fun isValidAddress(address: String): Boolean =
        try {
            PublicKey(address).toBase58() == address
        } catch (_: Exception) {
            false
        }

    companion object {
        const val MAINNET_RPC_URL = "https://wallet-api-5arl64l7ia-uc.a.run.app/helius"
    }
}
