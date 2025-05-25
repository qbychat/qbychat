/*
 * Copyright (c) 2025. All rights reserved.
 *
 * This file is a part of the QbyChat project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.cubewhy.qbychat

import org.bouncycastle.crypto.InvalidCipherTextException
import org.cubewhy.qbychat.shared.util.CipherUtil
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import java.security.SecureRandom

@SpringBootTest
@Profile("test")
@Import(TestcontainersConfiguration::class)
class QbychatApplicationTests {

    @Test
    fun `ChaCha20 encryption and decryption should be reversible`() {
        val key = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        val message = "Hello, qbychat!".toByteArray()
        val sessionId = 12345L
        val sequence = 1L

        val encrypted = CipherUtil.encryptMessage(key, message, sessionId, sequence)
        val decrypted = CipherUtil.decryptMessage(key, encrypted)

        assertArrayEquals(message, decrypted)
    }

    @Test
    fun `decrypt should fail if ciphertext is tampered (ChaCha20-Poly1305)`() {
        val key = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        val message = "Don't tamper me".toByteArray()
        val sessionId = 555L
        val sequence = 9L

        val encrypted = CipherUtil.encryptMessage(key, message, sessionId, sequence)

        // Tamper with ciphertext
        val tamperedBytes = encrypted.ciphertext.toByteArray().apply {
            this[0] = (this[0].toInt() xor 0xFF).toByte()
        }
        val tampered =
            encrypted.toBuilder().setCiphertext(com.google.protobuf.ByteString.copyFrom(tamperedBytes)).build()

        assertThrows<Exception> {
            CipherUtil.decryptMessage(key, tampered)
        }
    }

    @Test
    fun `decrypt should fail if ChaCha20 ciphertext is tampered`() {
        val key = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        val message = "This is a secret".toByteArray()
        val sessionId = 1234L
        val sequenceNumber = 1L

        val encrypted = CipherUtil.encryptMessage(
            chachaKey = key,
            message = message,
            sessionId = sessionId,
            sequenceNumber = sequenceNumber
        )

        val tamperedCiphertext = encrypted.ciphertext.toByteArray().apply {
            this[0] = (this[0].toInt() xor 0xFF).toByte()
        }

        val tamperedMessage = encrypted.toBuilder()
            .setCiphertext(com.google.protobuf.ByteString.copyFrom(tamperedCiphertext))
            .build()

        assertThrows<InvalidCipherTextException> {
            CipherUtil.decryptMessage(key, tamperedMessage)
        }
    }

    @Test
    fun `decrypt should fail if sessionId is tampered`() {
        val key: ByteArray = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        val message: ByteArray = "Test AAD tampering".toByteArray()
        val sessionId = 42L
        val sequenceNumber = 99L

        val encrypted = CipherUtil.encryptMessage(key, message, sessionId, sequenceNumber)

        val tampered = encrypted.toBuilder()
            .setSessionId(sessionId + 1) // bad AAD
            .build()

        assertThrows<InvalidCipherTextException> {
            CipherUtil.decryptMessage(key, tampered)
        }
    }

    @Test
    fun `decrypt should fail if sequenceNumber is tampered`() {
        val key: ByteArray = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        val message: ByteArray = "Test AAD tampering".toByteArray()
        val sessionId = 42L
        val sequenceNumber = 99L

        val encrypted = CipherUtil.encryptMessage(key, message, sessionId, sequenceNumber)

        val tampered = encrypted.toBuilder()
            .setSequenceNumber(sequenceNumber + 1) // bad AAD
            .build()

        assertThrows<InvalidCipherTextException> {
            CipherUtil.decryptMessage(key, tampered)
        }
    }

}
