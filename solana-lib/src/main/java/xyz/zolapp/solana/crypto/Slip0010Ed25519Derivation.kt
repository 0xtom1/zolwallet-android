package xyz.zolapp.solana.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * SLIP-0010 derivation for Ed25519 keys.
 *
 * Derives a Solana-compatible private key from a BIP39 seed using
 * the standard derivation path m/44'/501'/0'/0'.
 *
 * Reference: https://github.com/satoshilabs/slips/blob/master/slip-0010.md
 */
object Slip0010Ed25519Derivation {

    private const val ED25519_CURVE = "ed25519 seed"
    private const val HMAC_SHA512 = "HmacSHA512"
    private const val KEY_LENGTH = 32

    /**
     * Solana BIP-44 derivation path: m/44'/501'/0'/0'
     */
    private val SOLANA_PATH = intArrayOf(44, 501, 0, 0)

    /**
     * Derives a 32-byte Ed25519 private key from BIP39 seed bytes
     * using the standard Solana derivation path m/44'/501'/0'/0'.
     *
     * @param seed BIP39 seed bytes (typically 64 bytes)
     * @return 32-byte private key suitable for Ed25519 signing
     */
    fun deriveSolanaPrivateKey(seed: ByteArray): ByteArray {
        return deriveKey(seed, SOLANA_PATH)
    }

    /**
     * Derives a 32-byte Ed25519 private key from BIP39 seed bytes
     * using the specified hardened derivation path.
     *
     * All indices are automatically hardened (0x80000000 added).
     *
     * @param seed BIP39 seed bytes
     * @param path array of path indices (without hardening flag)
     * @return 32-byte private key
     */
    private fun deriveKey(seed: ByteArray, path: IntArray): ByteArray {
        // Master key generation
        var (key, chainCode) = hmacSha512(ED25519_CURVE.toByteArray(), seed)

        // Child key derivation for each path component
        for (index in path) {
            val hardenedIndex = index.toLong() or 0x80000000L
            val data = ByteArray(1 + KEY_LENGTH + 4) // 0x00 + key + index
            data[0] = 0x00
            key.copyInto(data, destinationOffset = 1)
            data[KEY_LENGTH + 1] = (hardenedIndex shr 24 and 0xFF).toByte()
            data[KEY_LENGTH + 2] = (hardenedIndex shr 16 and 0xFF).toByte()
            data[KEY_LENGTH + 3] = (hardenedIndex shr 8 and 0xFF).toByte()
            data[KEY_LENGTH + 4] = (hardenedIndex and 0xFF).toByte()

            val result = hmacSha512(chainCode, data)
            key = result.first
            chainCode = result.second
        }

        return key
    }

    /**
     * Computes HMAC-SHA512 and splits the result into two 32-byte halves.
     *
     * @return Pair of (IL = key material, IR = chain code)
     */
    private fun hmacSha512(key: ByteArray, data: ByteArray): Pair<ByteArray, ByteArray> {
        val mac = Mac.getInstance(HMAC_SHA512)
        mac.init(SecretKeySpec(key, HMAC_SHA512))
        val result = mac.doFinal(data)
        return Pair(
            result.copyOfRange(0, KEY_LENGTH),
            result.copyOfRange(KEY_LENGTH, KEY_LENGTH * 2)
        )
    }
}
