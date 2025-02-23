package org.cubewhy.qbychat.service

import com.google.protobuf.ByteString
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.springframework.web.reactive.socket.WebSocketSession

interface PacketProcessor {
    /**
     * Process packet
     *
     * @param method request method
     * @param payload payload
     * @param session websocket session
     * @param user issuer, null to anonymous
     * @return response message
     * */
    suspend fun process(method: String, payload: ByteString, session: WebSocketSession, user: User?): WebsocketResponse
}