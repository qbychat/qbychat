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
        return session.receive()
            .doFirst {
                // connected
                sessions[session.id] = session
            }.flatMap { message ->
                // resolve pbMessage
                val inputStream = message.payload.asInputStream()
                val pbMessage = if (session.aesKey != null) {
                    // decrypt message
                    val iv = readIvFromInputStream(inputStream) // parse iv
                    // TODO parse encryptedMessage
                    ServerboundMessage.parseFrom(
                        decryptInputStream(
                            inputStream,
                            session.aesKey!!,
                            iv
                        )
                    )
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