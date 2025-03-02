package org.cubewhy.qbychat.util

import com.google.protobuf.GeneratedMessage
import org.cubewhy.qbychat.entity.ClientInfo
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.WebsocketResponseType.COMMON
import org.cubewhy.qbychat.entity.WebsocketResponseType.HANDSHAKE
import org.cubewhy.qbychat.websocket.protocol.Protocol
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
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

fun GeneratedMessage.toProtobufResponse(ticket: String): Protocol.Response = Protocol.Response.newBuilder().apply {
    this.ticket = ticket
    this.payload = this@toProtobufResponse.toByteString()
}.build()

/**
 * Send response with encryption
 *
 * @param response websocket response
 * */
fun WebSocketSession.sendResponseWithEncryption(response: WebsocketResponse): Mono<Void> {
    // build messages
    val messages = mutableListOf<ByteArray>()
    response.response?.let { pbResponse ->
        messages.add(
            when (response.type) {
                COMMON -> Protocol.ClientboundMessage.newBuilder().apply {
                    response.userId?.let { this.account = it }
                    this.response = pbResponse.toProtobufResponse(response.ticket!!)
                }

                HANDSHAKE -> Protocol.ClientboundMessage.newBuilder().apply {
                    this.serverHandshake = pbResponse as Protocol.ServerHandshake
                }
            }.build().toByteArray()
        )
    }
    return this.send(messages.map { message ->
        this.binaryMessage { factory ->
            if (this.aesKey == null || response.type == HANDSHAKE) {
                // handshake packet should be unencrypted
                factory.wrap(message)
            } else {
                // encrypt
                factory.wrap(encryptAESGCM(message, this.aesKey!!))
            }
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
            factory.wrap(encryptAESGCM(payload, this.aesKey!!))
        }
    }.toMono())
}