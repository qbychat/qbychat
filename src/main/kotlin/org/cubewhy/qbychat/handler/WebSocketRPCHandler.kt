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

package org.cubewhy.qbychat.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.websocketResponseOf
import org.cubewhy.qbychat.service.PacketService
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.util.*
import org.cubewhy.qbychat.websocket.protocol.v1.EncryptedMessage
import org.cubewhy.qbychat.websocket.protocol.v1.RPCResponse
import org.cubewhy.qbychat.websocket.protocol.v1.ServerboundHandshake
import org.cubewhy.qbychat.websocket.protocol.v1.ServerboundMessage
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.netty.channel.AbortedException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketRPCHandler(
    private val packetService: PacketService,
    private val scope: CoroutineScope,
    private val sessionService: SessionService,
) : WebSocketHandler {
    companion object {
        private val logger = KotlinLogging.logger {}

        val sessions: ConcurrentHashMap<String, WebSocketSession> = ConcurrentHashMap()
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val window = SlidingWindow()

        return session.receive()
            .doFirst { sessions[session.id] = session }
            .concatMap { message ->
                val payloadDataBuffer = message.payload
                val inputStream = payloadDataBuffer.asInputStream(true)

                if (!session.handshakeStatus) {
                    // process handshake
                    return@concatMap handleHandshake(inputStream, session).then(Mono.empty())
                }

                // deserialize packet
                val serverboundMessage = processIncomingMessage(inputStream, session, window)
                    ?: return@concatMap Mono.empty()

                // process packet
                processPacket(serverboundMessage, session)
            }
            .flatMap { response ->
                sendResponseAndEvents(response, session)
            }
            .doOnError { e -> handleWebSocketError(e) }
            .doFinally { signalType -> handleSessionCleanup(signalType, session) }
            .then()
    }

    private fun handleHandshake(inputStream: InputStream, session: WebSocketSession): Mono<Void> {
        val handshakePacket = ServerboundHandshake.parseFrom(inputStream)
        return mono {
            packetService.processHandshake(handshakePacket, session)
        }.flatMap { clientboundHandshake ->
            session.sendWithEncryption(clientboundHandshake.toByteArray())
        }
    }

    private fun processIncomingMessage(
        inputStream: InputStream,
        session: WebSocketSession,
        window: SlidingWindow
    ): ServerboundMessage? {
        return if (session.chachaKey != null) {
            // Process encrypted message
            val encryptedMessage = EncryptedMessage.parseFrom(inputStream)

            if (encryptedMessage.sessionId != session.sessionId) return null

            val decryptedBytes = try {
                CipherUtil.decryptMessage(session.chachaKey!!, encryptedMessage)
            } catch (_: Exception) {
                // failed to decrypt or verify fail
                return null
            }

            if (!window.accept(encryptedMessage.sequenceNumber)) return null
            ServerboundMessage.parseFrom(decryptedBytes)
        } else {
            // Process unencrypted message
            ServerboundMessage.parseFrom(inputStream)
        }
    }

    private fun processPacket(
        serverboundMessage: ServerboundMessage,
        session: WebSocketSession
    ): Mono<WebsocketResponse> {
        return mono {
            try {
                packetService.process(serverboundMessage, session)
            } catch (e: Exception) {
                websocketResponseOf(
                    serverboundMessage.request.ticket.toByteArray(),
                    RPCResponse.Status.INTERNAL_ERROR,
                    e.message ?: "Internal Error"
                ).apply { this.ticket = serverboundMessage.request.ticket.toByteArray() }
            }
        }
    }

    private fun sendResponseAndEvents(response: WebsocketResponse, session: WebSocketSession): Mono<Void> {
        return session.sendResponseWithEncryption(response).then(
            mono {
                response.events.forEach { event ->
                    if (response.userId == null || !event.shared) {
                        // Push events locally
                        session.sendEventWithEncryption(event.eventMessage, null).awaitFirstOrNull()
                    } else {
                        // Push to the broker
                        sessionService.pushEvent(response.userId!!, event.eventMessage)
                    }
                }
            }
        ).then()
    }

    private fun handleWebSocketError(e: Throwable) {
        if (e !is AbortedException) {
            logger.error(e) { "WebSocket processing error" }
        }
    }

    private fun handleSessionCleanup(signalType: SignalType, session: WebSocketSession) {
        sessions.remove(session.id)
        val user = session.attributes["user"] as User?
        scope.launch {
            packetService.processDisconnect(signalType, session, user)
        }
    }

}