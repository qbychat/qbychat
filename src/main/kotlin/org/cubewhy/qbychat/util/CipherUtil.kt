package org.cubewhy.qbychat.util

import com.google.protobuf.ByteString
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.CipherParameters
import org.bouncycastle.crypto.KeyGenerationParameters
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.cubewhy.qbychat.websocket.protocol.v1.EncryptedMessage
import java.io.InputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CipherUtil {

    const val GCM_IV_LENGTH = 12
    const val GCM_TAG_LENGTH = 128

    fun generateX25519KeyPair(): AsymmetricCipherKeyPair = X25519KeyPairGenerator().apply {
        init(KeyGenerationParameters(SecureRandom(), 256))
    }.generateKeyPair()

    fun performKeyExchange(privateKey: CipherParameters, remotePublicKey: CipherParameters): ByteArray {
        val agreement = X25519Agreement()
        agreement.init(privateKey)

        val sharedSecret = ByteArray(agreement.agreementSize)
        agreement.calculateAgreement(remotePublicKey, sharedSecret, 0)
        return sharedSecret
    }

    fun deriveAesKeyFromX25519(
        sharedSecret: ByteArray,
        info: ByteArray,
        salt: ByteArray,
        keyLength: Int = 16
    ): SecretKey {
        val hkdf = HKDFBytesGenerator(SHA256Digest())

        val hkdfParameters = HKDFParameters(sharedSecret, salt, info)
        hkdf.init(hkdfParameters)

        val derivedKey = ByteArray(keyLength) // 16 bytes for AES-128, 32 for AES-256
        hkdf.generateBytes(derivedKey, 0, keyLength)

        return SecretKeySpec(derivedKey, "AES")
    }

    fun encryptMessage(
        aesKey: SecretKey,
        message: ByteArray,
        sessionId: Long,
        sequenceNumber: Long
    ): EncryptedMessage {
        val nonce = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(nonce)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec)

        // Use sessionId + sequenceNumber as AAD to protect them from tampering
        val aad = ByteBuffer.allocate(16)
            .putLong(sessionId)
            .putLong(sequenceNumber)
            .array()
        cipher.updateAAD(aad)

        val cipherText = cipher.doFinal(message) // includes tag

        return EncryptedMessage.newBuilder().apply {
            this.sessionId = sessionId
            this.sequenceNumber = sequenceNumber
            this.nonce = ByteString.copyFrom(nonce)
            this.ciphertext = ByteString.copyFrom(cipherText) // includes authTag in tail
        }.build()
    }

    fun decryptMessage(aesKey: SecretKey, encryptedMessage: EncryptedMessage): ByteArray {
        val nonce = encryptedMessage.nonce.toByteArray()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, nonce)
        cipher.init(Cipher.DECRYPT_MODE, aesKey, spec)

        // Set same AAD for validation
        val aad = ByteBuffer.allocate(16)
            .putLong(encryptedMessage.sessionId)
            .putLong(encryptedMessage.sequenceNumber)
            .array()
        cipher.updateAAD(aad)

        return cipher.doFinal(encryptedMessage.ciphertext.toByteArray()) // will throw AEADBadTagException if tampered
    }

    fun decryptInputStream(aesKey: SecretKey, inputStream: InputStream): ByteArray {
        // parse encrypted message
        return decryptMessage(aesKey, EncryptedMessage.parseFrom(inputStream))
    }
}

