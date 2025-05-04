package org.cubewhy.qbychat.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.service.PacketService
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.util.*
import org.cubewhy.qbychat.websocket.protocol.v1.EncryptedMessage
import org.cubewhy.qbychat.websocket.protocol.v1.ServerboundHandshake
import org.cubewhy.qbychat.websocket.protocol.v1.ServerboundMessage
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.netty.channel.AbortedException
import java.util.concurrent.ConcurrentHashMap

@Component
class WebsocketHandler(
    private val packetService: PacketService,
    private val scope: CoroutineScope,
    private val sessionService: SessionService,
) : WebSocketHandler {
    companion object {
        private val logger = KotlinLogging.logger {}

        val sessions: ConcurrentHashMap<String, WebSocketSession> = ConcurrentHashMap() // id:sessionObj
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val window = SlidingWindowManager()
        return session.receive()
            .doFirst {
                // connected
                sessions[session.id] = session
            }.concatMap { message ->
                // resolve pbMessage
                val payloadDataBuffer = message.payload

                val inputStream = payloadDataBuffer.asInputStream(true)
                if (!session.handshakeStatus) {
                    // this is the handshake packet
                    // parse packet
                    val handshakePacket = ServerboundHandshake.parseFrom(inputStream)
                    return@concatMap mono { packetService.processHandshake(handshakePacket, session) }
                }

                val pbMessage = if (session.chachaKey != null) {
                    // deserialize packet
                    val encryptedMessage = EncryptedMessage.parseFrom(inputStream)
                    // verify packet
                    if (encryptedMessage.sessionId != session.sessionId || window.accept(encryptedMessage.sequenceNumber)) {
                        // ignore this packet because session id doesn't match
                        return@concatMap Mono.empty()
                    }

                    // decrypt message
                    val decryptedBytes = CipherUtil.decryptMessage(chachaKey = session.chachaKey!!, encryptedMessage)
                    ServerboundMessage.parseFrom(decryptedBytes)
                } else {
                    // non-encrypted
                    ServerboundMessage.parseFrom(inputStream)
                }

                // process packet
                mono { packetService.process(pbMessage, session) }
            }.flatMap { response ->
                // send response to session
                session.sendResponseWithEncryption(response).then(
                    mono {
                        // publish events
                        response.events.forEach { event ->
                            if (response.userId == null || !event.shared) {
                                // push locally
                                session.sendEventWithEncryption(event.eventMessage, null).awaitFirstOrNull()
                            } else {
                                // push remotely
                                sessionService.pushEvent(response.userId!!, event.eventMessage)
                            }
                        }
                    }
                )
            }.doOnError { e ->
                if (e !is AbortedException) {
                    // ignore session disconnected
                    logger.error(e) { "WebSocket processing error" }
                }
            }.doFinally { signalType ->
                // remove session from session map
                sessions.remove(session.id)
                // remove session id and close session
                val user = session.attributes["user"] as User?
                // remove session
                scope.launch {
                    packetService.processDisconnect(signalType, session, user)
                }
            }.then()
    }
}