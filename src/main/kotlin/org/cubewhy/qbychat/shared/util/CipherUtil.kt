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

package org.cubewhy.qbychat.shared.util

import com.google.protobuf.ByteString
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.CipherParameters
import org.bouncycastle.crypto.KeyGenerationParameters
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.cubewhy.qbychat.websocket.protocol.v1.EncryptedMessage
import org.cubewhy.qbychat.websocket.protocol.v1.encryptedMessage
import java.nio.ByteBuffer
import java.security.SecureRandom

object CipherUtil {
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

    private fun hkdfExpand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
        val hmac = HMac(SHA256Digest())
        hmac.init(KeyParameter(prk))

        val block = ByteArray(hmac.macSize)
        var result = ByteArray(0)
        var i = 1

        while (result.size < length) {
            hmac.update(block, 0, block.size)
            hmac.update(info, 0, info.size)
            hmac.update(byteArrayOf(i.toByte()), 0, 1)

            hmac.doFinal(block, 0)

            result += block.take(minOf(block.size, length - result.size)).toByteArray()
            i++
        }

        return result
    }

    fun deriveChaCha20Key(sharedSecret: ByteArray, info: ByteArray): ByteArray {
        return hkdfExpand(sharedSecret, info, 32)
    }

    fun encryptMessage(
        chachaKey: ByteArray,
        message: ByteArray,
        sessionId: Long,
        sequenceNumber: Long
    ): EncryptedMessage {
        // Generate a random nonce (12 bytes for ChaCha20)
        val nonce = ByteArray(12)
        SecureRandom().nextBytes(nonce)

        // Use sessionId + sequenceNumber as AAD to protect them from tampering
        val aad = ByteBuffer.allocate(16)
            .putLong(0, sessionId)
            .putLong(8, sequenceNumber)
            .array()

        // Prepare the ChaCha20 cipher
        val cipher = ChaCha20Poly1305()
        val params = AEADParameters(KeyParameter(chachaKey), 128, nonce, aad)
        cipher.init(true, params)

        // Encrypt the message
        val cipherText = ByteArray(cipher.getOutputSize(message.size))
        val len = cipher.processBytes(message, 0, message.size, cipherText, 0)
        cipher.doFinal(cipherText, len)

        // Return the encrypted message
        return encryptedMessage {
            this.sessionId = sessionId
            this.sequenceNumber = sequenceNumber
            this.nonce = ByteString.copyFrom(nonce)
            this.ciphertext = ByteString.copyFrom(cipherText)
        }
    }

    fun decryptMessage(
        chachaKey: ByteArray,
        encryptedMessage: EncryptedMessage
    ): ByteArray {
        // Extract components
        val nonce = encryptedMessage.nonce.toByteArray()
        val cipherText = encryptedMessage.ciphertext.toByteArray()
        val sessionId = encryptedMessage.sessionId
        val sequenceNumber = encryptedMessage.sequenceNumber

        // Reconstruct AAD
        val aad = ByteBuffer.allocate(16)
            .putLong(0, sessionId)
            .putLong(8, sequenceNumber)
            .array()

        // Prepare ChaCha20-Poly1305 cipher for decryption
        val cipher = ChaCha20Poly1305()
        val params = AEADParameters(KeyParameter(chachaKey), 128, nonce, aad)
        cipher.init(false, params)

        // Decrypt
        val plainText = ByteArray(cipher.getOutputSize(cipherText.size))
        val len = cipher.processBytes(cipherText, 0, cipherText.size, plainText, 0)
        cipher.doFinal(plainText, len)

        return plainText
    }
}

