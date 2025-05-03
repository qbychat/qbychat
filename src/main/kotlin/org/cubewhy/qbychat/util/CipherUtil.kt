package org.cubewhy.qbychat.util

import com.google.protobuf.ByteString
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.CipherParameters
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.cubewhy.qbychat.websocket.protocol.v1.EncryptedMessage
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CipherUtil {

    const val AES_KEY_SIZE = 16
    const val GCM_IV_LENGTH = 12
    const val GCM_TAG_LENGTH = 128

    private const val AES_ALGORITHM = "AES"

    fun generateX25519KeyPair(): AsymmetricCipherKeyPair = X25519KeyPairGenerator().generateKeyPair()

    fun performKeyExchange(privateKey: CipherParameters, remotePublicKey: CipherParameters): ByteArray {
        val agreement = X25519Agreement()
        agreement.init(privateKey)

        val sharedSecret = ByteArray(agreement.agreementSize)
        agreement.calculateAgreement(remotePublicKey, sharedSecret, 0)
        return sharedSecret
    }

    fun generateAESKey(sharedSecret: ByteArray): SecretKey {
        return SecretKeySpec(sharedSecret.copyOfRange(0, 16), AES_ALGORITHM)
    }

    fun encryptMessage(
        sharedSecret: ByteArray,
        message: ByteArray,
        sessionId: Long,
        sequenceNumber: Long
    ): EncryptedMessage {
        val aesKey = SecretKeySpec(sharedSecret.copyOf(AES_KEY_SIZE), "AES")
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

    fun decryptMessage(sharedSecret: ByteArray, encryptedMessage: EncryptedMessage): ByteArray {
        val aesKey = SecretKeySpec(sharedSecret.copyOf(AES_KEY_SIZE), "AES")
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

}

