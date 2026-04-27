package util

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object NewAESUtil {

    private const val ALGORITHM = "AES"
    const val SECRET_KEY = "1234567890123456"


    fun encrypt(data: String, secret: String): String {
        val key = SecretKeySpec(secret.toByteArray(), ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val encrypted = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(data: String, secret: String): String {
        val key = SecretKeySpec(secret.toByteArray(), ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key)

        val decoded = Base64.getDecoder().decode(data)
        return String(cipher.doFinal(decoded))
    }
}