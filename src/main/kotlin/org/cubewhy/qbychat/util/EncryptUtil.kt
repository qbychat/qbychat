package org.cubewhy.qbychat.util

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

fun getPublicKeyFromBytes(publicKeyBytes: ByteArray): PublicKey {
    val keyFactory = KeyFactory.getInstance("RSA")
    val keySpec = X509EncodedKeySpec(publicKeyBytes)
    return keyFactory.generatePublic(keySpec)
}

fun generateRSAKeyPair(keySize: Int = 4096): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(keySize)
    return keyPairGenerator.generateKeyPair()
}

fun decryptInputStream(inputStream: InputStream, privateKey: PrivateKey): ByteArray {
    // Initialize the cipher for decryption
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding") // Use appropriate padding if needed
    cipher.init(Cipher.DECRYPT_MODE, privateKey)

    // Read the input stream in chunks and decrypt
    val buffer = ByteArray(256) // RSA block size is typically 256 bytes for 2048-bit keys
    val outputStream = ByteArrayOutputStream()

    // Read the encrypted data in chunks
    var bytesRead = inputStream.read(buffer)
    while (bytesRead != -1) {
        // Decrypt the chunk
        val decryptedChunk = cipher.doFinal(buffer, 0, bytesRead)
        outputStream.write(decryptedChunk)
        bytesRead = inputStream.read(buffer)
    }

    return outputStream.toByteArray() // Return the decrypted data as a byte array
}

fun encryptWithPublicKey(data: ByteArray, publicKey: PublicKey): ByteArray {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    return cipher.doFinal(data)
}