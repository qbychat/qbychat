package org.cubewhy.qbychat.service.impl

import com.google.protobuf.kotlin.toByteString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.qbychat.entity.*
import org.cubewhy.qbychat.service.PacketProcessor
import org.cubewhy.qbychat.service.PacketService
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.service.UserService
import org.cubewhy.qbychat.util.*
import org.cubewhy.qbychat.security.WebsocketSecurityFilter
import org.cubewhy.qbychat.websocket.protocol.Protocol
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType

@Service
class PacketServiceImpl(
    private val sessionService: SessionService,
    private val userService: UserService,
    private val handlers: MutableList<out PacketProcessor>,
    private val websocketSecurityFilter: WebsocketSecurityFilter
) : PacketService {
    private val handlerMap = mutableMapOf<String, PacketProcessor>()

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        handlers.forEach { handler ->
            logger.info { "Register packet processor ${handler.javaClass.interfaces[0].name}" }
            handlerMap[handler.javaClass.interfaces[0].name] = handler
        }
        logger.info { "${handlers.size} packet processors registered" }
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
                platform = clientHandshake.clientInfo.platform.number,
                installationId = clientHandshake.clientInfo.installationId
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
            // find handler
            val processor = this.handlerMap[request.service]
            val response = if (processor == null) {
                logger.warn { "Session ${session.id} tried to access to an invalid service (${request.service})" }
                emptyWebsocketResponse()
            } else {
                try {
                    // do filter
                    websocketSecurityFilter.doFilter(request.service, request.method, session, user)
                    if (user != null) {
                        logger.info { "Session ${session.id} (user ${user.username}) request ${request.service}:${request.method}" }
                    } else {
                        logger.info { "Session ${session.id} request ${request.service}:${request.method}" }
                    }
                    // process packet
                    processor.process(request.method, request.payload, session, user)
                } catch (e: RuntimeException) {
                    // unauthorized
                    emptyWebsocketResponse()
                }
            }.apply {
                // add ticket to response
                // this -> response
                this.ticket = request.ticket
                // add user id to response
                user?.let {
                    this.userId = it.id!!
                }
            }
            return response
        }
        return emptyWebsocketResponse()
    }

    override suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User?) {
        // remove sessions from session store
        logger.info { "Session ${session.id} disconnected" }
        sessionService.removeWebsocketSessions(session)
    }
}