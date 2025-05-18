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

package org.cubewhy.qbychat.interfaces.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.application.service.PacketService
import org.cubewhy.qbychat.application.service.SessionManager
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection
import org.cubewhy.qbychat.infrastructure.transport.WebsocketConnection
import org.cubewhy.qbychat.shared.model.WebsocketResponse
import org.cubewhy.qbychat.shared.model.websocketResponseOf
import org.cubewhy.qbychat.shared.util.CipherUtil
import org.cubewhy.qbychat.shared.util.SlidingWindow
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

@Component
class WebSocketRPCHandler(
    private val packetService: PacketService,
    private val scope: CoroutineScope,
    private val sessionManager: SessionManager,
) : WebSocketHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val window = SlidingWindow()
        val connection = WebsocketConnection(session)

        return session.receive()
            .doFirst { sessionManager.addLocalSession(connection) }
            .concatMap { message ->
                val payloadDataBuffer = message.payload
                val inputStream = payloadDataBuffer.asInputStream(true)

                if (!connection.metadata.handshakeStatus) {
                    // process handshake
                    return@concatMap handleHandshake(inputStream, connection).then(Mono.empty())
                }

                // deserialize packet
                val serverboundMessage = processIncomingMessage(inputStream, connection, window)
                    ?: return@concatMap Mono.empty()

                // process packet
                processPacket(serverboundMessage, connection)
            }
            .flatMap { response ->
                sendResponseAndEvents(response, connection)
            }
            .doOnError { e -> handleWebSocketError(e) }
            .doFinally { signalType -> handleSessionCleanup(signalType, connection) }
            .then()
    }

    private fun handleHandshake(inputStream: InputStream, connection: WebsocketConnection): Mono<Void> {
        val handshakePacket = ServerboundHandshake.parseFrom(inputStream)
        return mono {
            packetService.processHandshake(handshakePacket, connection)
        }.flatMap { clientboundHandshake ->
            mono { connection.send(clientboundHandshake.toByteArray()) }.then()
        }
    }

    private fun processIncomingMessage(
        inputStream: InputStream,
        connection: ClientConnection<*>,
        window: SlidingWindow
    ): ServerboundMessage? {
        return if (connection.metadata.chachaKey != null) {
            // Process encrypted message
            val encryptedMessage = EncryptedMessage.parseFrom(inputStream)

            if (encryptedMessage.sessionId != connection.metadata.encrpytionSessionId) return null

            val decryptedBytes = try {
                CipherUtil.decryptMessage(connection.metadata.chachaKey!!, encryptedMessage)
            } catch (e: Exception) {
                // failed to decrypt or verify fail
                logger.warn(e) { "Failed to decrypt message (bad cipherText)" }
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
        connection: WebsocketConnection
    ): Mono<WebsocketResponse> {
        return mono {
            (try {
                packetService.process(serverboundMessage, connection)
            } catch (e: Exception) {
                websocketResponseOf(
                    serverboundMessage.request.ticket.toByteArray(),
                    RPCResponse.Status.INTERNAL_ERROR,
                    e.message ?: "Internal Error"
                )
            }).apply { this.ticket = serverboundMessage.request.ticket.toByteArray() }
        }
    }

    private fun sendResponseAndEvents(response: WebsocketResponse, connection: WebsocketConnection): Mono<Void> {
        return mono {
            connection.sendResponse(response)
            // non-shared events was processed in sendResponse
            response.events.filter { it.shared }.forEach { event ->
                // Push to the broker
                sessionManager.pushEvent(response.userId!!, event.eventMessage)
            }
        }.then()
    }

    private fun handleWebSocketError(e: Throwable) {
        if (e !is AbortedException) {
            logger.error(e) { "WebSocket processing error" }
        }
    }

    private fun handleSessionCleanup(signalType: SignalType, connection: WebsocketConnection) {
        sessionManager.removeLocalSession(connection.id)
        scope.launch {
            packetService.processDisconnect(signalType, connection)
        }
    }

}