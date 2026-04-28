package util

import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object NewAESUtil {


    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_LENGTH = 128
    const val SECRET_KEY = "1234567890123456"

    fun encrypt(data: String, secret: String): String {
        val key = SecretKeySpec(secret.toByteArray(), "AES")

        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val encrypted = cipher.doFinal(data.toByteArray())

        // combine IV + encrypted
        val combined = iv + encrypted

        return Base64.getUrlEncoder().encodeToString(combined)
    }

    fun decrypt(data: String, secret: String): String {
        val key = SecretKeySpec(secret.toByteArray(), "AES")

        val decoded = Base64.getUrlDecoder().decode(data)

        val iv = decoded.copyOfRange(0, IV_SIZE)
        val encrypted = decoded.copyOfRange(IV_SIZE, decoded.size)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val decrypted = cipher.doFinal(encrypted)

        return String(decrypted)
    }
}