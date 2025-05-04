package org.cubewhy.qbychat.service.impl

import com.google.protobuf.kotlin.toByteString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.config.QbyChatProperties
import org.cubewhy.qbychat.entity.emptyWebsocketResponse
import org.cubewhy.qbychat.entity.handshakeResponseOf
import org.cubewhy.qbychat.service.PacketService
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.util.CipherUtil
import org.cubewhy.qbychat.util.chachaKey
import org.cubewhy.qbychat.util.handshakeStatus
import org.cubewhy.qbychat.util.sessionId
import org.cubewhy.qbychat.websocket.protocol.v1.ClientboundHandshake
import org.cubewhy.qbychat.websocket.protocol.v1.ServerEncryptionInfo
import org.cubewhy.qbychat.websocket.protocol.v1.ServerboundHandshake
import org.cubewhy.qbychat.websocket.protocol.v1.ServerboundMessage
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType
import java.security.SecureRandom

@Service
class PacketServiceImpl(
    private val sessionService: SessionService,
    private val qbyChatProperties: QbyChatProperties
) : PacketService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processHandshake(
        handshake: ServerboundHandshake,
        session: WebSocketSession
    ): WebsocketResponse {
        // mark shook hand
        session.handshakeStatus = true
        if (!handshake.hasEncryptionInfo()) {
            // client doesn't want this session be encrypted.
            if (qbyChatProperties.websocket.requireEncryption) {
                // the server requires client use an encrypted connection
                // close session
                session.close(CloseStatus.create(1001, "This instance requires an encrypted connection.")).awaitFirstOrNull()
                return emptyWebsocketResponse()
            }
            return handshakeResponseOf(ClientboundHandshake.getDefaultInstance())
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
        return handshakeResponseOf(ClientboundHandshake.newBuilder().apply {
            encryptionInfo = serverEncryptionInfo
        }.build())
    }

    override suspend fun process(message: ServerboundMessage, session: WebSocketSession): WebsocketResponse {
        // TODO process requests
        return emptyWebsocketResponse()
    }

    override suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User?) {
        // remove sessions from session store
        logger.debug { "Session ${session.id} disconnected" }
        sessionService.removeWebsocketSessions(session)
    }

}