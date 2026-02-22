package xyz.zolapp.market.model

data class TokenPrice(
    val mint: String,
    val priceUsd: Double,
    val timestampMs: Long,
)
