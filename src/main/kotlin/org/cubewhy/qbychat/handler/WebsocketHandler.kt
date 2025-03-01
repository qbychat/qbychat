package org.cubewhy.qbychat.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.service.PacketService
import org.cubewhy.qbychat.util.aesKey
import org.cubewhy.qbychat.util.decryptInputStream
import org.cubewhy.qbychat.util.readIvFromInputStream
import org.cubewhy.qbychat.util.sendWithEncryption
import org.cubewhy.qbychat.websocket.protocol.Protocol
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.netty.channel.AbortedException

@Component
class WebsocketHandler(
    private val packetService: PacketService,
    private val scope: CoroutineScope,
) : WebSocketHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        return session.receive()
            .flatMap { message ->
                // resolve pbMessage
                val inputStream = message.payload.asInputStream()
                val pbMessage = if (session.aesKey != null) {
                    // decrypt message
                    // [iv(12 bytes)][encrypted data]
                    val iv = readIvFromInputStream(inputStream) // parse iv
                    Protocol.ServerboundMessage.parseFrom(
                        decryptInputStream(
                            inputStream,
                            session.aesKey!!,
                            iv
                        )
                    )
                } else {
                    // non-encrypted
                    Protocol.ServerboundMessage.parseFrom(inputStream)
                }
                // process packet
                mono { packetService.process(pbMessage, session) }
            }.flatMap { response ->
                // send response to session
                session.sendWithEncryption(response)
            }.doOnError { e ->
                if (e !is AbortedException) {
                    // ignore session disconnected
                    logger.error(e) { "WebSocket processing error" }
                }
            }.doFinally { signalType ->
                // remove session id and close session
                val user = session.attributes["user"] as User?
                // remove session
                scope.launch {
                    packetService.processDisconnect(signalType, session, user)
                }
            }.then()
    }
}