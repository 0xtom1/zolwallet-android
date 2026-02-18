package xyz.zolapp.solana.crypto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.sol4k.Keypair

/**
 * Provides access to the Solana keypair derived from the BIP39 seed.
 *
 * The keypair is derived on-demand and cached in memory.
 * No separate key storage — reuses the existing encrypted BIP39 seed.
 */
interface SolanaKeypairProvider {
    /**
     * Returns the Solana keypair derived from the wallet's BIP39 seed.
     */
    suspend fun getKeypair(): Keypair

    /**
     * Returns the Solana public key (Base58 address) derived from the wallet's BIP39 seed.
     */
    suspend fun getAddress(): String
}

/**
 * Implementation that accepts a seed provider function to decouple
 * from Zcash-specific storage.
 *
 * @param seedProvider function that returns the BIP39 seed bytes (typically 64 bytes)
 */
class SolanaKeypairProviderImpl(
    private val seedProvider: suspend () -> ByteArray
) : SolanaKeypairProvider {

    private val mutex = Mutex()
    private var cachedKeypair: Keypair? = null

    override suspend fun getKeypair(): Keypair =
        mutex.withLock {
            cachedKeypair ?: withContext(Dispatchers.Default) {
                val seed = seedProvider()
                val privateKey = Slip0010Ed25519Derivation.deriveSolanaPrivateKey(seed)
                Keypair.fromSecretKey(privateKey)
            }.also { cachedKeypair = it }
        }

    override suspend fun getAddress(): String = getKeypair().publicKey.toBase58()
}
