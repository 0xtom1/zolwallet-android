package xyz.zolapp.solana.crypto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sol4k.Keypair

/**
 * Provides access to Solana keypairs derived from the BIP39 seed.
 *
 * Keys are derived on-demand and never cached in memory,
 * following the same pattern as Zcash's ZashiSpendingKeyDataSource.
 */
interface SolanaKeypairProvider {
    /**
     * Returns the Solana keypair derived from the wallet's BIP39 seed.
     *
     * @param accountIndex the account index in the derivation path m/44'/501'/{accountIndex}'/0'
     */
    suspend fun getKeypair(accountIndex: Int = 0): Keypair

    /**
     * Returns the Solana public key (Base58 address) derived from the wallet's BIP39 seed.
     *
     * @param accountIndex the account index in the derivation path m/44'/501'/{accountIndex}'/0'
     */
    suspend fun getAddress(accountIndex: Int = 0): String
}

/**
 * Implementation that accepts a seed provider function to decouple
 * from Zcash-specific storage. Derives keys fresh on every call —
 * no key material is retained in memory.
 *
 * @param seedProvider function that returns the BIP39 seed bytes (typically 64 bytes)
 */
class SolanaKeypairProviderImpl(
    private val seedProvider: suspend () -> ByteArray
) : SolanaKeypairProvider {

    override suspend fun getKeypair(accountIndex: Int): Keypair =
        withContext(Dispatchers.Default) {
            val seed = seedProvider()
            val privateKey = Slip0010Ed25519Derivation.deriveSolanaPrivateKey(seed, accountIndex)
            Keypair.fromSecretKey(privateKey)
        }

    override suspend fun getAddress(accountIndex: Int): String =
        getKeypair(accountIndex).publicKey.toBase58()
}
