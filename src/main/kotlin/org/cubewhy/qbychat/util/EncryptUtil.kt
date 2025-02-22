package org.cubewhy.qbychat.util

import java.io.IOException
import java.io.InputStream
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

fun getECDHPublicKeyFromBytes(publicKeyBytes: ByteArray): PublicKey {
    val keyFactory = KeyFactory.getInstance("EC")
    val keySpec = X509EncodedKeySpec(publicKeyBytes)
    return keyFactory.generatePublic(keySpec)
}

fun generateECDHKeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance("EC")
    keyPairGenerator.initialize(256)
    return keyPairGenerator.generateKeyPair()
}

fun computeSharedSecret(privateKey: KeyPair, publicKey: PublicKey): ByteArray {
    val keyAgreement = KeyAgreement.getInstance("ECDH")
    keyAgreement.init(privateKey.private)
    keyAgreement.doPhase(publicKey, true)
    return keyAgreement.generateSecret()
}

fun bytesToAESKey(keyBytes: ByteArray): SecretKey {
    return SecretKeySpec(keyBytes, "AES")
}

fun encryptAESGCM(data: ByteArray, secretKey: SecretKey): ByteArray {
    val iv = ByteArray(12)
    SecureRandom().nextBytes(iv)

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
    val ciphertext = cipher.doFinal(data)

    return iv + ciphertext
}

fun hkdfSha256(inputKeyMaterial: ByteArray, salt: ByteArray, info: ByteArray, outputLength: Int): ByteArray {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(salt, "HmacSHA256"))

    val prk = mac.doFinal(inputKeyMaterial)
    mac.init(SecretKeySpec(prk, "HmacSHA256"))
    mac.update(info + 0x01)
    return mac.doFinal().copyOf(outputLength)
}

fun readIvFromInputStream(inputStream: InputStream): ByteArray {
    val iv = ByteArray(12)
    if (inputStream.read(iv) != iv.size) throw IOException("Invalid payload")
    return iv
}

fun decryptInputStream(inputStream: InputStream, secretKey: SecretKey, iv: ByteArray): InputStream {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

    return CipherInputStream(inputStream, cipher)
}
