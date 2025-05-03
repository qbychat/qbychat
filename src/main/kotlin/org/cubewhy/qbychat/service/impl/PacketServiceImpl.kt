package org.cubewhy.qbychat.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.emptyWebsocketResponse
import org.cubewhy.qbychat.service.PacketService
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.websocket.protocol.v1.ServerboundMessage
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType

@Service
class PacketServiceImpl(
    private val sessionService: SessionService,
) : PacketService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun process(message: ServerboundMessage, session: WebSocketSession): WebsocketResponse {
        return emptyWebsocketResponse()
    }

    override suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User?) {
        // remove sessions from session store
        logger.debug { "Session ${session.id} disconnected" }
        sessionService.removeWebsocketSessions(session)
    }
}