package org.cubewhy.qbychat.util

import com.google.protobuf.GeneratedMessage
import org.cubewhy.qbychat.entity.ClientInfo
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.WebsocketResponseType.COMMON
import org.cubewhy.qbychat.entity.WebsocketResponseType.HANDSHAKE
import org.cubewhy.qbychat.websocket.protocol.v1.ClientboundMessage
import org.cubewhy.qbychat.websocket.protocol.v1.Response
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.atomic.AtomicLong
import javax.crypto.SecretKey

var WebSocketSession.aesKey: SecretKey?
    get() =
        this.attributes["aesk"] as SecretKey?
    set(value) {
        this.attributes["aesk"] = value
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

fun GeneratedMessage.toProtobufResponse(ticket: String): Response = Response.newBuilder().apply {
    this.ticket = ticket
    this.payload = this@toProtobufResponse.toByteString()
}.build()

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
    response.response?.let { pbResponse ->
        messages.add(
            when (response.type) {
                COMMON -> ClientboundMessage.newBuilder().apply {
                    response.userId?.let { this.userId = it }
                    this.response = pbResponse.toProtobufResponse(response.ticket!!)
                }.build()

                HANDSHAKE -> pbResponse
            }.toByteArray()
        )
    }
    return this.send(messages.map { message ->
        this.binaryMessage { factory ->
            if (this.aesKey == null || response.type == HANDSHAKE) {
                // handshake packet should be unencrypted
                message
            } else {
                // encrypt
                CipherUtil.encryptMessage(
                    aesKey = this.aesKey!!,
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
        if (this.aesKey == null) {
            // unencrypted
            factory.wrap(payload)
        } else {
            // do encrypt
            factory.wrap(
                CipherUtil.encryptMessage(
                    this.aesKey!!,
                    payload,
                    this.sessionId!!,
                    this.s2cPacketCounter.incrementAndGet()
                ).toByteArray()
            )
        }
    }.toMono())
}