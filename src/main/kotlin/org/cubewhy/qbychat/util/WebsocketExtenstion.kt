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

package org.cubewhy.qbychat.util

import com.google.protobuf.GeneratedMessage
import org.cubewhy.qbychat.entity.ClientInfo
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.websocket.protocol.v1.ClientboundMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.atomic.AtomicLong

var WebSocketSession.chachaKey: ByteArray?
    get() =
        this.attributes["chachakey"] as ByteArray?
    set(value) {
        this.attributes["chachakey"] = value
    }

var WebSocketSession.handshakeStatus: Boolean
    get() =
        this.attributes["handshake"] as Boolean? ?: false
    set(value) {
        this.attributes["handshake"] = value
    }

var WebSocketSession.clientInfo: ClientInfo?
    get() =
        this.attributes["clientInfo"] as ClientInfo?
    set(value) {
        this.attributes["clientInfo"] = value
    }

var WebSocketSession.sessionId: Long?
    get() =
        this.attributes["sessionId"] as Long?
    set(value) {
        this.attributes["sessionId"] = value
    }

val WebSocketSession.s2cPacketCounter: AtomicLong
    get() {
        val key = "packet-counter"
        val counter = this.attributes[key] as AtomicLong?
        if (counter == null) {
            this.attributes[key] = AtomicLong(0)
        }
        return this.attributes[key] as AtomicLong
    }

/**
 * Send response with encryption
 *
 * @param response websocket response
 * */
fun WebSocketSession.sendResponseWithEncryption(response: WebsocketResponse): Mono<Void> {
    if (!this.isOpen) {
        // session closed
        return Mono.empty()
    }
    // build messages
    val messages = mutableListOf<ByteArray>()
    response.buildRPCResponse().let { pbResponse ->
        messages.add(
            ClientboundMessage.newBuilder().apply {
                response.userId?.let { this.userId = it }
                this.response = pbResponse
            }.build().toByteArray()
        )
    }

    return this.send(messages.map { message ->
        this.binaryMessage { factory ->
            if (this.chachaKey == null || response.clientboundHandshake != null) {
                // handshake packet should be unencrypted
                message
            } else {
                // encrypt
                CipherUtil.encryptMessage(
                    chachaKey = this.chachaKey!!,
                    message = message,
                    sessionId = this.sessionId!!,
                    sequenceNumber = this.s2cPacketCounter.incrementAndGet()
                ).toByteArray()
            }.let { factory.wrap(it) }
        }
    }.toFlux())
}

fun WebSocketSession.sendEventWithEncryption(event: GeneratedMessage, userId: String?): Mono<Void> {
    return this.sendWithEncryption(protobufEventOf(event, userId).toByteArray())
}

fun WebSocketSession.sendWithEncryption(payload: ByteArray): Mono<Void> {
    // encrypt & send
    return this.send(this.binaryMessage { factory ->
        if (this.chachaKey == null) {
            // unencrypted
            factory.wrap(payload)
        } else {
            // do encrypt
            factory.wrap(
                CipherUtil.encryptMessage(
                    this.chachaKey!!,
                    payload,
                    this.sessionId!!,
                    this.s2cPacketCounter.incrementAndGet()
                ).toByteArray()
            )
        }
    }.toMono())
}