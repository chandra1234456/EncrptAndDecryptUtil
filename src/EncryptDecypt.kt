import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * AESUtils is a utility object that provides **AES-GCM encryption and decryption** using a password.
 *
 * Instead of manually managing keys, it derives a SecretKey from a password using PBKDF2 (Password-Based Key Derivation Function 2).
 * Each encryption generates a **random salt** and **IV** for security.
 *
 * Features:
 * - AES-GCM encryption (128-bit key, 128-bit authentication tag)
 * - Password-based key derivation (PBKDF2WithHmacSHA256)
 * - Random salt and IV per encryption
 * - Base64-encoded output containing salt + IV + ciphertext
 */
object AESUtils {

    // AES key size in bits
    private const val AES_KEY_SIZE = 128

    // GCM authentication tag length in bits
    private const val GCM_TAG_LENGTH = 128

    // PBKDF2 iterations for key derivation
    private const val ITERATION_COUNT = 65536

    // Salt size in bytes
    private const val SALT_SIZE = 16

    // IV size in bytes for AES-GCM
    private const val IV_SIZE = 12

    /**
     * Generates a random salt.
     * @return ByteArray of size SALT_SIZE
     */
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_SIZE)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Derives a SecretKey from a password and salt using PBKDF2.
     *
     * @param password The user-provided password
     * @param salt Randomly generated salt
     * @return SecretKey object suitable for AES encryption
     */
    fun generateKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, AES_KEY_SIZE)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    /**
     * Encrypts a plain text string using AES-GCM with a password.
     * Automatically generates a random salt and IV.
     *
     * @param plainText The string to encrypt
     * @param password The password used to derive the AES key
     * @return Base64-encoded string containing salt + IV + ciphertext
     */
    fun encrypt(plainText: String, password: String): String {
        val salt = generateSalt()
        val key = generateKeyFromPassword(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        // Generate random IV for this encryption
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)

        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Combine salt + IV + ciphertext for storage
        val combined = salt + iv + cipherText
        return Base64.getEncoder().encodeToString(combined)
    }

    /**
     * Decrypts a Base64-encoded cipher text string using the same password.
     *
     * @param cipherText Base64-encoded string containing salt + IV + ciphertext
     * @param password The password used to derive the AES key
     * @return The decrypted plain text string
     */
    fun decrypt(cipherText: String, password: String): String {
        val decoded = Base64.getDecoder().decode(cipherText)

        // Extract salt, IV, and actual ciphertext
        val salt = decoded.sliceArray(0 until SALT_SIZE)
        val iv = decoded.sliceArray(SALT_SIZE until SALT_SIZE + IV_SIZE)
        val actualCipher = decoded.sliceArray(SALT_SIZE + IV_SIZE until decoded.size)

        val key = generateKeyFromPassword(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val plainBytes = cipher.doFinal(actualCipher)

        return String(plainBytes, Charsets.UTF_8)
    }
}

/**
 * Example usage of AESUtils.
 */
fun main() {
    val password = "MyStrongPassword123!"
    val secretText = "Hello IntelliJ Encryption with Password"

    // Encrypt the text
    val encrypted = AESUtils.encrypt(secretText, password)
    println("Encrypted: $encrypted")

    // Decrypt the text
    val decrypted = AESUtils.decrypt(encrypted, password)
    println("Decrypted: $decrypted")
}

