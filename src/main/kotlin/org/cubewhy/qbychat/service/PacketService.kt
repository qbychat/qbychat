package org.cubewhy.qbychat.service

import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.websocket.protocol.v1.ServerboundMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType

interface PacketService {
    suspend fun process(
        message: ServerboundMessage,
        session: WebSocketSession
    ): WebsocketResponse

    suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User?)
}