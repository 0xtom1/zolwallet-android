package xyz.zolapp.market.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.zolapp.market.cache.PriceCache
import xyz.zolapp.market.model.TokenPrice
import xyz.zolapp.market.provider.HeliusPriceProvider

class PriceRepository(
    private val priceProvider: HeliusPriceProvider,
    private val cache: PriceCache,
) {
    /**
     * Get price for a single token. Returns cached value if fresh, otherwise fetches from Helius.
     */
    suspend fun getPrice(mint: String): TokenPrice? =
        withContext(Dispatchers.IO) {
            cache.get(mint)?.let { return@withContext it }
            val price = priceProvider.getPrice(mint) ?: return@withContext null
            cache.put(price)
            price
        }

    /**
     * Get prices for multiple tokens. Only fetches from Helius for mints not in cache.
     */
    suspend fun getPrices(mints: List<String>): Map<String, TokenPrice> =
        withContext(Dispatchers.IO) {
            val result = mutableMapOf<String, TokenPrice>()
            val stale = mutableListOf<String>()

            for (mint in mints) {
                val cached = cache.get(mint)
                if (cached != null) {
                    result[mint] = cached
                } else {
                    stale.add(mint)
                }
            }

            if (stale.isNotEmpty()) {
                val fresh = priceProvider.getPrices(stale)
                for ((mint, price) in fresh) {
                    cache.put(price)
                }
                result.putAll(fresh)
            }

            result
        }

    /**
     * Read-only cache lookup — no network call. Use for synchronous UI reads.
     */
    fun getCachedPrice(mint: String): TokenPrice? = cache.get(mint)

    /**
     * Force-fetch prices from Helius, ignoring cache. Updates the cache with fresh data.
     */
    suspend fun refreshPrices(mints: List<String>): Map<String, TokenPrice> =
        withContext(Dispatchers.IO) {
            val fresh = priceProvider.getPrices(mints)
            for ((_, price) in fresh) {
                cache.put(price)
            }
            fresh
        }
}
