package org.cubewhy.qbychat.service.impl

import com.google.protobuf.kotlin.toByteString
import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.emptyWebsocketResponse
import org.cubewhy.qbychat.entity.handshakeResponse
import org.cubewhy.qbychat.service.PacketService
import org.cubewhy.qbychat.util.*
import org.cubewhy.qbychat.websocket.protocol.Protocol
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType

@Service
class PacketServiceImpl : PacketService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun process(message: Protocol.ServerboundMessage, session: WebSocketSession): WebsocketResponse {
        if (message.hasClientHandshake() && !session.handshakeStatus) {
            // handle handshake
            // save client public key to session
            logger.info { "Session ${session.id} handshake" }
            val clientHandshake = message.clientHandshake
            // todo save client info
            if (!clientHandshake.hasPublicKey()) {
                // encryption is disabled on clientside
                logger.info { "Encryption of session ${session.id} is disabled (no ECDH public key provided)" }
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
        return emptyWebsocketResponse()
    }

    override suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User?) {
    }
}