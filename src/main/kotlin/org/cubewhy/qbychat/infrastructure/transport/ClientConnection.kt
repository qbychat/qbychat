/*
 *  Copyright (c) 2025. All rights reserved.
 *  This file is a part of the QbyChat project
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.cubewhy.qbychat.infrastructure.transport

import com.google.protobuf.GeneratedMessage
import org.cubewhy.qbychat.shared.model.WebsocketResponse
import org.cubewhy.qbychat.shared.util.CipherUtil
import org.cubewhy.qbychat.shared.util.protobuf.protobufEventOf
import org.cubewhy.qbychat.websocket.protocol.v1.ClientboundMessage
import java.util.concurrent.atomic.AtomicLong

/**
 * Abstract base class representing a client connection.
 *
 * @param T the type of the native underlying connection
 */
abstract class ClientConnection<T> {
    /**
     * The underlying native connection instance.
     */
    abstract val nativeConnection: T

    /**
     * Metadata associated with this connection, such as encryption keys and session information.
     */
    val metadata: ConnectionMetadata = ConnectionMetadata()

    abstract val id: String

    /**
     * Indicates whether the connection is currently open.
     */
    abstract val isOpen: Boolean

    /**
     * Sends a single binary message over the connection.
     *
     * @param message the message payload as a byte array
     */
    abstract suspend fun send(message: ByteArray)

    /**
     * Sends multiple binary messages over the connection.
     * The default implementation sends messages sequentially one by one.
     *
     * @param messages a list of message payloads as byte arrays
     */
    open suspend fun send(messages: List<ByteArray>) {
        messages.forEach { message -> this.send(message) }
    }

    /**
     * Closes the connection with the given code and an optional reason.
     *
     * @param code the close status code
     * @param reason the reason for closing, or null if none
     */
    abstract suspend fun close(code: Int, reason: String?)


    /**
     * Sends a binary payload over the connection with optional encryption.
     * If the connection metadata contains a `chachaKey`, the payload will be encrypted before sending.
     * Otherwise, it will be sent as-is.
     *
     * @param payload the original message payload to send
     */
    suspend fun sendWithEncryption(payload: ByteArray) {
        // encrypt & send
        if (this.metadata.chachaKey == null) {
            // send unencrypted
            this.send(payload)
        } else {
            val encryptedPayload = CipherUtil.encryptMessage(
                this.metadata.chachaKey!!,
                payload,
                this.metadata.encrpytionSessionId!!,
                this.metadata.s2cPacketCounter.incrementAndGet()
            ).toByteArray()
            this.send(encryptedPayload)
        }
    }

    /**
     * Sends a [WebsocketResponse] over the connection, encrypting if needed.
     *
     * The method first checks if the connection is open.
     * Then it builds the main protobuf response message, followed by any included event messages.
     * All messages are sent as a batch.
     *
     * @param response the websocket response object to send
     */
    suspend fun sendResponse(response: WebsocketResponse) {
        if (!this.isOpen) {
            // session closed
            return
        }
        // build response
        val clientboundMessage = response.buildRPCResponse().let { pbResponse ->
            ClientboundMessage.newBuilder().apply {
                response.userId?.let { this.userId = it }
                this.response = pbResponse
            }.build().toByteArray()
        }
        if (response.events.isEmpty()) {
            // no events
            return this.send(clientboundMessage)
        }
        val messages = mutableListOf<ByteArray>()
        messages.add(clientboundMessage)
        // build events
        messages.addAll(response.events.filter { !it.shared }.map { it.buildProtobufMessage(response.userId).toByteArray() })
        // send bulk
        this.send(messages)
    }

    /**
     * Sends a single event message with encryption if applicable.
     *
     * This method serializes the given protobuf [event], associates it with the optional [userId],
     * and sends the resulting message over the connection using encryption when available.
     *
     * @param event the protobuf event message to send
     * @param userId optional user ID associated with the event
     */
    suspend fun sendEvent(event: GeneratedMessage, userId: String?) {
        this.sendWithEncryption(protobufEventOf(event, userId).toByteArray())
    }

    data class ConnectionMetadata(
        var chachaKey: ByteArray? = null,
        var handshakeStatus: Boolean = false,
        var clientId: String? = null,
        var encrpytionSessionId: Long? = null,
        val s2cPacketCounter: AtomicLong = AtomicLong(0)
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ConnectionMetadata

            if (handshakeStatus != other.handshakeStatus) return false
            if (encrpytionSessionId != other.encrpytionSessionId) return false
            if (!chachaKey.contentEquals(other.chachaKey)) return false
            if (clientId != other.clientId) return false
            if (s2cPacketCounter != other.s2cPacketCounter) return false

            return true
        }

        override fun hashCode(): Int {
            var result = handshakeStatus.hashCode()
            result = 31 * result + (encrpytionSessionId?.hashCode() ?: 0)
            result = 31 * result + (chachaKey?.contentHashCode() ?: 0)
            result = 31 * result + (clientId?.hashCode() ?: 0)
            result = 31 * result + s2cPacketCounter.hashCode()
            return result
        }
    }
}