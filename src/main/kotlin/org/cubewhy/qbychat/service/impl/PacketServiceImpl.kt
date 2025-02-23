package org.cubewhy.qbychat.service.impl

import com.google.protobuf.kotlin.toByteString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.qbychat.entity.*
import org.cubewhy.qbychat.service.PacketService
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.service.UserService
import org.cubewhy.qbychat.util.*
import org.cubewhy.qbychat.websocket.protocol.Protocol
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType

@Service
class PacketServiceImpl(
    private val sessionService: SessionService,
    private val userService: UserService
) : PacketService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun process(message: Protocol.ServerboundMessage, session: WebSocketSession): WebsocketResponse {
        // load user
        val user = userService.loadUser(message)
        if (message.hasClientHandshake() && !session.handshakeStatus) {
            // handle handshake
            // save client public key to session
            logger.info { "Session ${session.id} handshake" }
            val clientHandshake = message.clientHandshake
            // save client info
            session.clientInfo = ClientInfo(
                name = clientHandshake.clientInfo.name,
                version = clientHandshake.clientInfo.version,
                platform = clientHandshake.clientInfo.platform
            )
            if (!clientHandshake.hasPublicKey()) {
                // encryption is disabled on clientside
                logger.info { "Encryption of session ${session.id} is disabled (no ECDH public key provided)" }
                session.handshakeStatus = true
                return handshakeResponse(Protocol.ServerHandshake.getDefaultInstance())
            }
            logger.info { "Session ${session.id} key exchange" }
            // key exchange
            // generate server keypair
            val keyPair = generateECDHKeyPair()
            // calc shared secret
            val secret =
                computeSharedSecret(keyPair, getECDHPublicKeyFromBytes(clientHandshake.publicKey.toByteArray()))
            val aesKey = hkdfSha256(
                secret,
                clientHandshake.aesKeySalt.toByteArray(),
                clientHandshake.aesKeyInfo.toByteArray(),
                clientHandshake.aesKeyLength
            )
            // save AES key to session
            session.aesKey = bytesToAESKey(aesKey)
            logger.info { "Session ${session.id} key exchange finished" }
            session.handshakeStatus = true
            val response = Protocol.ServerHandshake.newBuilder().apply {
                this.publicKey = keyPair.public.encoded.toByteString()
            }.build()
            return handshakeResponse(response)
        }
        if (user != null) {
            if (!sessionService.isOnSession(session, user)) {
                logger.warn { "Session ${session.id} send invalid packet (account ${message.account} not exist on this session)" }
                session.close().awaitFirstOrNull()
                return emptyWebsocketResponse()
            }
        }
        if (message.hasRequest()) {
            val request = message.request
            val response =  when (request.service) {
                "org.cubewhy.qbychat.service.UserService" -> userService.process(request.method, request.payload, session, user)
                else -> emptyWebsocketResponse()
            }
            // add ticket to response
            response.ticket = request.ticket
            return response
        }
        return emptyWebsocketResponse()
    }

    override suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User?) {
        // remove sessions from session store
        sessionService.removeWebsocketSessions(session)
    }
}