package xyz.zolapp.solana.model

/**
 * Represents a fungible token (including native SOL) returned from the Helius DAS API.
 *
 * @param mint Token mint address (empty string for native SOL)
 * @param name Human-readable token name
 * @param symbol Token ticker symbol
 * @param imageUrl URL to the token icon/logo (nullable)
 * @param balance Raw token balance (in smallest unit)
 * @param decimals Number of decimal places for the token
 * @param uiAmount Human-readable balance (balance / 10^decimals)
 * @param pricePerToken USD price per single token (nullable if unavailable)
 * @param valueUsd Total USD value of held tokens (nullable if price unavailable)
 */
data class SplTokenInfo(
    val mint: String,
    val name: String,
    val symbol: String,
    val imageUrl: String?,
    val balance: Long,
    val decimals: Int,
    val uiAmount: Double,
    val pricePerToken: Double?,
    val valueUsd: Double?,
)
