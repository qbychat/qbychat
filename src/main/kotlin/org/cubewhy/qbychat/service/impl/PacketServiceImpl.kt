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

package org.cubewhy.qbychat.service.impl

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.kotlin.toByteString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.cubewhy.qbychat.annotations.rpc.RPCContext
import org.cubewhy.qbychat.annotations.rpc.RPCHandlerRegistry
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.config.QbyChatProperties
import org.cubewhy.qbychat.entity.errorWebsocketResponseOf
import org.cubewhy.qbychat.entity.websocketResponseOf
import org.cubewhy.qbychat.exception.WebsocketForbidden
import org.cubewhy.qbychat.exception.WebsocketNotFound
import org.cubewhy.qbychat.exception.WebsocketUnauthorized
import org.cubewhy.qbychat.service.PacketService
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.util.CipherUtil
import org.cubewhy.qbychat.util.chachaKey
import org.cubewhy.qbychat.util.handshakeStatus
import org.cubewhy.qbychat.util.sessionId
import org.cubewhy.qbychat.websocket.protocol.v1.*
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType
import java.security.SecureRandom

@Service
class PacketServiceImpl(
    private val sessionService: SessionService,
    private val qbyChatProperties: QbyChatProperties,
    private val rpcHandlerRegistry: RPCHandlerRegistry
) : PacketService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processHandshake(
        handshake: ServerboundHandshake,
        session: WebSocketSession
    ): ClientboundHandshake? {
        // mark shook hand
        session.handshakeStatus = true
        if (!handshake.hasEncryptionInfo()) {
            // client doesn't want this session be encrypted.
            if (qbyChatProperties.websocket.requireEncryption) {
                // the server requires client use an encrypted connection
                // close session
                session.close(CloseStatus.create(1001, "This instance requires an encrypted connection."))
                    .awaitFirstOrNull()
                return null
            }
            return ClientboundHandshake.getDefaultInstance()
        }
        // do key exchange
        val clientEncryptionInfo = handshake.encryptionInfo
        // generate keypair
        val keypair = CipherUtil.generateX25519KeyPair()
        // compute sharedSecret
        val sharedSecret = CipherUtil.performKeyExchange(
            privateKey = keypair.private,
            remotePublicKey = X25519PublicKeyParameters(clientEncryptionInfo.publicKey.toByteArray())
        )
        // compute ChaCha20 key
        val chachaKey = CipherUtil.deriveChaCha20Key(
            sharedSecret,
            info = clientEncryptionInfo.chacha20KeyInfo.toByteArray()
        )
        // save ChaCha20 key
        session.chachaKey = chachaKey
        // generate session id (random long)
        session.sessionId = SecureRandom().nextLong()
        // build handshake response
        val serverEncryptionInfo = ServerEncryptionInfo.newBuilder().apply {
            this.publicKey = (keypair.public as X25519PublicKeyParameters).encoded.toByteString()
            this.sessionId = session.sessionId!!
        }.build()
        return ClientboundHandshake.newBuilder().apply {
            encryptionInfo = serverEncryptionInfo
        }.build()
    }

    override suspend fun process(message: ServerboundMessage, session: WebSocketSession): WebsocketResponse {
        val user = sessionService.getUser(session)
        return try {
            val response = rpcHandlerRegistry.invokeHandler(
                message.request.method, RPCContext(
                    user = user,
                    session = session
                )
            )
            when (response) {
                is WebsocketResponse -> {
                    response
                }

                is GeneratedMessage -> {
                    websocketResponseOf(response.toByteArray())
                }

                is ByteArray -> {
                    websocketResponseOf(response)
                }

                else -> {
                    // bad handler
                    errorWebsocketResponseOf(
                        RPCResponse.Status.INTERNAL_ERROR,
                        "The handler doesn't response a correct type (need WebsocketResponse or GeneratedMessage)"
                    )
                }
            }
        } catch (e: WebsocketUnauthorized) {
            errorWebsocketResponseOf(
                RPCResponse.Status.UNAUTHORIZED,
                e.message
            )
        } catch (e: WebsocketForbidden) {
            errorWebsocketResponseOf(
                RPCResponse.Status.FORBIDDEN,
                e.message
            )
        } catch (e: WebsocketNotFound) {
            errorWebsocketResponseOf(
                RPCResponse.Status.NOT_FOUND,
                e.message
            )
        } catch (e: RuntimeException) {
            logger.error(e) { "Failed to handle packet" }
            errorWebsocketResponseOf(
                RPCResponse.Status.INTERNAL_ERROR,
                "Internal Server Error"
            )
        }
    }

    override suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User?) {
        // remove sessions from session store
        logger.debug { "Session ${session.id} disconnected" }
        sessionService.removeWebsocketSessions(session)
    }

}