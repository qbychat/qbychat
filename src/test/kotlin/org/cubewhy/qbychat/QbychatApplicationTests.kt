package org.cubewhy.qbychat

import org.cubewhy.qbychat.util.CipherUtil
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.boot.test.context.SpringBootTest
import java.io.ByteArrayInputStream
import javax.crypto.SecretKey

@SpringBootTest
class QbychatApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    fun `test generate X25519 key pair`() {
        val keyPair = CipherUtil.generateX25519KeyPair()
        assertNotNull(keyPair)
        assertNotNull(keyPair.public)
        assertNotNull(keyPair.private)
    }

    @Test
    fun `test perform key exchange`() {
        val keyPair1 = CipherUtil.generateX25519KeyPair()
        val keyPair2 = CipherUtil.generateX25519KeyPair()

        val sharedSecret1 = CipherUtil.performKeyExchange(keyPair1.private, keyPair2.public)
        val sharedSecret2 = CipherUtil.performKeyExchange(keyPair2.private, keyPair1.public)

        assertArrayEquals(sharedSecret1, sharedSecret2) // Shared secrets should be identical
    }

    @Test
    fun `test derive AES key from X25519 shared secret`() {
        val keyPair1 = CipherUtil.generateX25519KeyPair()
        val keyPair2 = CipherUtil.generateX25519KeyPair()

        val sharedSecret = CipherUtil.performKeyExchange(keyPair1.private, keyPair2.public)

        // Derive AES key (16 bytes for AES-128)
        val aesKey: SecretKey = CipherUtil.deriveAesKeyFromX25519(sharedSecret, ByteArray(0), ByteArray(0), 16)
        assertNotNull(aesKey)
        assertEquals(16, aesKey.encoded.size) // Ensure the AES key length is 16 bytes for AES-128
    }

    @Test
    fun `test encrypt and decrypt message`() {
        val keyPair1 = CipherUtil.generateX25519KeyPair()
        val keyPair2 = CipherUtil.generateX25519KeyPair()

        val sharedSecret = CipherUtil.performKeyExchange(keyPair1.private, keyPair2.public)
        val aesKey = CipherUtil.deriveAesKeyFromX25519(sharedSecret, ByteArray(0), ByteArray(0), 16)

        val message = "Hello, world!".toByteArray()
        val sessionId = 123L
        val sequenceNumber = 1L

        val encryptedMessage = CipherUtil.encryptMessage(aesKey, message, sessionId, sequenceNumber)

        // Now decrypt the message and check if it's the same
        val decryptedMessage = CipherUtil.decryptMessage(aesKey, encryptedMessage)
        assertArrayEquals(message, decryptedMessage) // The decrypted message should match the original
    }

    @Test
    fun `test decrypt message with tampered data`() {
        val keyPair1 = CipherUtil.generateX25519KeyPair()
        val keyPair2 = CipherUtil.generateX25519KeyPair()

        val sharedSecret = CipherUtil.performKeyExchange(keyPair1.private, keyPair2.public)
        val aesKey = CipherUtil.deriveAesKeyFromX25519(sharedSecret, ByteArray(0), ByteArray(0), 16)

        val message = "Hello, world!".toByteArray()
        val sessionId = 123L
        val sequenceNumber = 1L

        val encryptedMessage = CipherUtil.encryptMessage(aesKey, message, sessionId, sequenceNumber)

        // Tamper with the ciphertext
        val tamperedCiphertext = encryptedMessage.ciphertext.toByteArray().copyOf()
        tamperedCiphertext[0] = (tamperedCiphertext[0] + 1).toByte() // Modify the first byte

        // Create a new EncryptedMessage with tampered data
        val tamperedEncryptedMessage = encryptedMessage.toBuilder().setCiphertext(com.google.protobuf.ByteString.copyFrom(tamperedCiphertext)).build()

        try {
            // Try to decrypt the tampered message, should throw AEADBadTagException
            CipherUtil.decryptMessage(aesKey, tamperedEncryptedMessage)
            fail("Expected AEADBadTagException to be thrown")
        } catch (e: javax.crypto.AEADBadTagException) {
            // Expected exception
        }
    }

    @Test
    fun `test decrypt message from input stream`() {
        val keyPair1 = CipherUtil.generateX25519KeyPair()
        val keyPair2 = CipherUtil.generateX25519KeyPair()

        val sharedSecret = CipherUtil.performKeyExchange(keyPair1.private, keyPair2.public)
        val aesKey = CipherUtil.deriveAesKeyFromX25519(sharedSecret, ByteArray(0), ByteArray(0), 16)

        val message = "Hello from input stream!".toByteArray()
        val sessionId = 456L
        val sequenceNumber = 2L

        val encryptedMessage = CipherUtil.encryptMessage(aesKey, message, sessionId, sequenceNumber)

        // Create an input stream from the encrypted message
        val inputStream = ByteArrayInputStream(encryptedMessage.toByteArray())

        // Now decrypt the message from the input stream
        val decryptedMessage = CipherUtil.decryptInputStream(aesKey, inputStream)

        assertArrayEquals(message, decryptedMessage) // The decrypted message should match the original
    }

}
