package xyz.zolapp.market.cache

import xyz.zolapp.market.model.TokenPrice
import java.util.concurrent.ConcurrentHashMap

class PriceCache(
    private val ttlMs: Long = DEFAULT_TTL_MS,
) {
    private val cache = ConcurrentHashMap<String, TokenPrice>()

    fun get(mint: String): TokenPrice? {
        val entry = cache[mint] ?: return null
        if (System.currentTimeMillis() - entry.timestampMs > ttlMs) {
            cache.remove(mint)
            return null
        }
        return entry
    }

    fun put(price: TokenPrice) {
        cache[price.mint] = price
    }

    fun putAll(prices: Map<String, TokenPrice>) {
        cache.putAll(prices)
    }

    fun clear() {
        cache.clear()
    }

    companion object {
        const val DEFAULT_TTL_MS = 600_000L // 10 minutes — matches Helius server-side cache
    }
}
