package org.cubewhy.qbychat.service

import org.cubewhy.qbychat.entity.Session
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.UserWebsocketSession
import org.springframework.web.reactive.socket.WebSocketSession

interface SessionService {
    suspend fun findSessions(user: User): List<UserWebsocketSession>
    suspend fun isAuthorized(session: WebSocketSession): Boolean
    suspend fun saveWebsocketSession(websocketSession: WebSocketSession, user: User)
    suspend fun removeWebsocketSessions(websocketSession: WebSocketSession)
    suspend fun isOnSession(session: WebSocketSession, user: User): Boolean
    suspend fun createSession(user: User, session: WebSocketSession): Session
    suspend fun isSessionValid(sessionId: String): Boolean
    suspend fun regenerateToken(sessionId: String, webSocketSession: WebSocketSession): String?
}