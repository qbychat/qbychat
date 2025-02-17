package org.cubewhy.qbychat.service

import org.cubewhy.celestial.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.websocket.protocol.Protocol
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType

interface PacketService {
    suspend fun process(
        message: Protocol.ServerboundMessage,
        session: WebSocketSession
    ): WebsocketResponse

    suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User)
}