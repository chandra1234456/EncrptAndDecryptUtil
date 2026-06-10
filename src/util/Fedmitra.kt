package util

import java.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Throws(Exception::class)
fun getEncryptValue(text: String, key: String?): String {
    var modifiedKey = key

    modifiedKey = if (!modifiedKey.isNullOrEmpty()) {
        when {
            modifiedKey.length == 10 -> {
                (modifiedKey + modifiedKey + modifiedKey + modifiedKey).substring(0, 32)
            }
            modifiedKey.length == 36 -> {
                modifiedKey.substring(0, 32)
            }
            else -> {
                modifiedKey.uppercase().substring(0, 32)
            }
        }
    } else {
        throw IllegalArgumentException("Key cannot be null or empty")
    }

    val data = modifiedKey.substring(0, 16)

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")

    val keyBytes = ByteArray(32)
    val keyData = modifiedKey.toByteArray(StandardCharsets.UTF_8)
    val len = minOf(keyData.size, keyBytes.size)

    System.arraycopy(keyData, 0, keyBytes, 0, len)

    val keySpec = SecretKeySpec(keyBytes, "AES")

    val ivBytes = data.toByteArray(StandardCharsets.UTF_8)

    val gcmParameterSpec = GCMParameterSpec(128, ivBytes)

    cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec)

    val encryptedBytes = cipher.doFinal(text.toByteArray(StandardCharsets.UTF_8))

    return Base64.getEncoder().encodeToString(encryptedBytes)
}

@Throws(Exception::class)
fun getDecryptValue(cipherText: String, key: String?): String {
    var modifiedKey = key

    modifiedKey = if (!modifiedKey.isNullOrEmpty()) {
        when {
            modifiedKey.length == 10 -> {
                (modifiedKey + modifiedKey + modifiedKey + modifiedKey).substring(0, 32)
            }
            modifiedKey.length == 36 -> {
                modifiedKey.substring(0, 32)
            }
            else -> {
                modifiedKey.uppercase().substring(0, 32)
            }
        }
    } else {
        throw IllegalArgumentException("Key cannot be null or empty")
    }

    val data = modifiedKey.substring(0, 16)

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")

    val keyBytes = ByteArray(32)
    val keyData = modifiedKey.toByteArray(StandardCharsets.UTF_8)
    val len = minOf(keyData.size, keyBytes.size)

    System.arraycopy(keyData, 0, keyBytes, 0, len)

    val secretKeySpec = SecretKeySpec(keyBytes, "AES")

    val ivBytes = data.toByteArray(StandardCharsets.UTF_8)

    val gcmParameterSpec = GCMParameterSpec(128, ivBytes)

    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec)

    val decodedBytes = Base64.getDecoder().decode(cipherText)

    return String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8)
}