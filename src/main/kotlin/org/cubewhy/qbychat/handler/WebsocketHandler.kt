package org.cubewhy.qbychat.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.service.PacketService
import org.cubewhy.qbychat.util.decryptInputStream
import org.cubewhy.qbychat.util.sendWithEncryption
import org.cubewhy.qbychat.util.serverPrivateKey
import org.cubewhy.qbychat.websocket.protocol.Protocol
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.netty.channel.AbortedException

@Component
class WebsocketHandler(
    private val packetService: PacketService
) : WebSocketHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        return session.receive()
            .flatMap { message ->
                // resolve pbMessage
                val pbMessage = if (session.serverPrivateKey != null) {
                    // decrypt message
                    Protocol.ServerboundMessage.parseFrom(
                        decryptInputStream(
                            message.payload.asInputStream(),
                            session.serverPrivateKey!!
                        )
                    )
                } else {
                    // non-encrypted
                    Protocol.ServerboundMessage.parseFrom(message.payload.asInputStream())
                }
                // process packet
                mono { packetService.process(pbMessage, session) }
            }.flatMap { response ->
                // send response to session
                session.sendWithEncryption(response)
            }
            .doOnError { e ->
                if (e !is AbortedException) {
                    // ignore session disconnected
                    logger.error(e) { "WebSocket processing error" }
                }
            }.doFinally { signalType ->
                // remove session id and close session
                val user = session.attributes["user"] as User?
                // remove session
                mono {
                    packetService.processDisconnect(signalType, session, user)
                }.publishOn(Schedulers.boundedElastic()).subscribe()
            }.then()
    }
}