package org.cubewhy.qbychat.service.impl

import org.cubewhy.celestial.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.service.PacketService
import org.cubewhy.qbychat.websocket.protocol.Protocol
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType

@Service
class PacketServiceImpl : PacketService {
    override suspend fun process(message: Protocol.ServerboundMessage, session: WebSocketSession): WebsocketResponse {
        if (message.hasHandshake()) {
            // handle handshake
        }
        TODO("wip")
    }

    override suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User) {
        TODO("Not yet implemented")
    }
}