package org.cubewhy.qbychat.util

import com.google.protobuf.kotlin.toByteString
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.cubewhy.qbychat.websocket.protocol.v1.EncryptedMessage
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object CipherUtil {

    private const val AES_ALGORITHM = "AES"
    private const val AES_CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"

    fun generateX25519KeyPair(): AsymmetricCipherKeyPair = X25519KeyPairGenerator().generateKeyPair()

    // X25519 密钥交换
    fun performKeyExchange(privateKey: ByteArray, publicKey: ByteArray): ByteArray {
        val privateKeyParams = X25519PrivateKeyParameters(privateKey, 0)
        val publicKeyParams = X25519PublicKeyParameters(publicKey, 0)
        TODO("wip")
//        val sharedSecret = X25519.computeAgreement(privateKeyParams, publicKeyParams)
//
//        return sharedSecret
    }

    // 使用共享密钥生成 AES 密钥
    fun generateAESKey(sharedSecret: ByteArray): SecretKey {
        return SecretKeySpec(sharedSecret.copyOfRange(0, 16), AES_ALGORITHM)
    }

    // 加密消息
    fun encryptMessage(sharedSecret: ByteArray, message: ByteArray): EncryptedMessage {
        val aesKey = generateAESKey(sharedSecret)
        val cipher = Cipher.getInstance(AES_CIPHER_TRANSFORMATION)
        val nonce = ByteArray(12) // GCM需要12字节的nonce
        SecureRandom().nextBytes(nonce)
        cipher.init(Cipher.ENCRYPT_MODE, aesKey)

        val ciphertext = cipher.doFinal(message)
        val authTag = cipher.getIV() // 保存 IV 作为 auth_tag

        return EncryptedMessage.newBuilder().apply {
            this.sessionId = System.currentTimeMillis()
            this.sequenceNumber = 1
            this.nonce = nonce.toByteString()
            this.ciphertext = ciphertext.toByteString()
            this.authTag = authTag.toByteString()
        }.build()
    }

    fun decryptMessage(sharedSecret: ByteArray, encryptedMessage: EncryptedMessage): ByteArray {
        val aesKey = generateAESKey(sharedSecret)
        val cipher = Cipher.getInstance(AES_CIPHER_TRANSFORMATION)
        val ivSpec = javax.crypto.spec.IvParameterSpec(encryptedMessage.nonce.toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec)

        return cipher.doFinal(encryptedMessage.ciphertext.toByteArray())
    }

}

