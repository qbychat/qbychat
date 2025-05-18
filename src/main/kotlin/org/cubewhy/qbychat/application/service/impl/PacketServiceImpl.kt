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

package org.cubewhy.qbychat.application.service.impl

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.kotlin.toByteString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.cubewhy.qbychat.application.service.PacketService
import org.cubewhy.qbychat.application.service.SessionManager
import org.cubewhy.qbychat.config.properties.QbyChatProperties
import org.cubewhy.qbychat.domain.repository.UserRepository
import org.cubewhy.qbychat.exception.WebsocketBadRequest
import org.cubewhy.qbychat.exception.WebsocketForbidden
import org.cubewhy.qbychat.exception.WebsocketNotFound
import org.cubewhy.qbychat.exception.WebsocketUnauthorized
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection
import org.cubewhy.qbychat.shared.annotations.rpc.RPCContext
import org.cubewhy.qbychat.shared.annotations.rpc.RPCHandlerRegistry
import org.cubewhy.qbychat.shared.model.WebsocketResponse
import org.cubewhy.qbychat.shared.model.errorWebsocketResponseOf
import org.cubewhy.qbychat.shared.model.websocketResponseOf
import org.cubewhy.qbychat.shared.util.CipherUtil
import org.cubewhy.qbychat.websocket.protocol.v1.*
import org.springframework.stereotype.Service
import reactor.core.publisher.SignalType
import java.security.SecureRandom

@Service
class PacketServiceImpl(
    private val sessionManager: SessionManager,
    private val qbyChatProperties: QbyChatProperties,
    private val rpcHandlerRegistry: RPCHandlerRegistry,
    private val userRepository: UserRepository
) : PacketService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processHandshake(
        handshake: ServerboundHandshake,
        connection: ClientConnection<*>
    ): ClientboundHandshake? {
        // mark shook hand
        connection.metadata.handshakeStatus = true
        if (!handshake.hasEncryptionInfo()) {
            // client doesn't want this session be encrypted.
            if (qbyChatProperties.websocket.requireEncryption) {
                // the server requires client use an encrypted connection
                // close session
                connection.close(1001, "This instance requires an encrypted connection.")
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
        connection.metadata.chachaKey = chachaKey
        // generate session id (random long)
        connection.metadata.encrpytionSessionId = SecureRandom().nextLong()
        // build handshake response
        val serverEncryptionInfo = ServerEncryptionInfo.newBuilder().apply {
            this.publicKey = (keypair.public as X25519PublicKeyParameters).encoded.toByteString()
            this.sessionId = connection.metadata.encrpytionSessionId!!
        }.build()
        return ClientboundHandshake.newBuilder().apply {
            this.encryptionInfo = serverEncryptionInfo
        }.build()
    }

    override suspend fun process(message: ServerboundMessage, connection: ClientConnection<*>): WebsocketResponse {
        val user = message.userId.takeIf { it != null }?.let {
            userRepository.findById(message.userId).awaitFirstOrNull()
        }
        if (user != null) {
            if (!sessionManager.isOnSession(connection, user)) {
                return errorWebsocketResponseOf(RPCResponse.Status.UNAUTHORIZED, "Unauthorized")
            }
        }
        return try {
            val response = rpcHandlerRegistry.invokeHandler(
                message.request.method, RPCContext(
                    user = user,
                    connection = connection,
                    payload = message.request.payload.toByteArray()
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

                null -> {
                    errorWebsocketResponseOf(RPCResponse.Status.INTERNAL_ERROR, "The handler responded \"null\"")
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
        } catch (e: WebsocketBadRequest) {
            errorWebsocketResponseOf(
                RPCResponse.Status.BAD_REQUEST,
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

    override suspend fun processDisconnect(signalType: SignalType, connection: ClientConnection<*>) {
        // remove sessions from session store
        sessionManager.removeSession(connection)
    }
}