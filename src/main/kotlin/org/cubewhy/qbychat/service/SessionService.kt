package org.cubewhy.qbychat.service

import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.UserWebsocketSession
import org.springframework.web.reactive.socket.WebSocketSession

interface SessionService {
    suspend fun findSessions(user: User): List<UserWebsocketSession>
    suspend fun saveSession(websocketSession: WebSocketSession, user: User)
    suspend fun isOnSession(session: WebSocketSession, user: User): Boolean
}