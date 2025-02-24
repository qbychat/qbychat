package org.cubewhy.qbychat.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import org.cubewhy.qbychat.entity.Session
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.UserWebsocketSession
import org.cubewhy.qbychat.repository.SessionRepository
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.util.Const
import org.cubewhy.qbychat.util.clientInfo
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class SessionServiceImpl(
    private val userWebsocketSessionReactiveRedisTemplate: ReactiveRedisTemplate<String, UserWebsocketSession>,
    private val sessionRepository: SessionRepository,
) : SessionService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun saveWebsocketSession(websocketSession: WebSocketSession, user: User) {
        logger.info { "Saving session for ${user.username} at connection ${websocketSession.id}" }
        val wsSessionObject = UserWebsocketSession(
            websocketId = websocketSession.id,
            userId = user.id!!
        )
        userWebsocketSessionReactiveRedisTemplate.opsForSet().add(Const.USER_WEBSOCKET_SESSION_STORE, wsSessionObject)
            .awaitFirst()
    }

    override suspend fun isOnSession(session: WebSocketSession, user: User): Boolean {
        return findSessions(user).any { it.websocketId == session.id }
    }

    override suspend fun findSessions(user: User): List<UserWebsocketSession> =
        userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .filter { it.userId == user.id }
            .collectList()
            .awaitLast()

    override suspend fun isAuthorized(session: WebSocketSession): Boolean {
        return userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .any { it.websocketId == session.id }
            .awaitLast()
    }

    override suspend fun createSession(user: User, session: WebSocketSession): Session {
        val session1 = Session(
            user = user.id!!,
            clientName = session.clientInfo!!.name,
            clientVersion = session.clientInfo!!.version,
            platform = session.clientInfo!!.platform
        )
        return sessionRepository.save(session1).awaitFirst()
    }

    override suspend fun removeWebsocketSessions(websocketSession: WebSocketSession) {
        userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .filter { it.websocketId == websocketSession.id }
            .flatMap { session ->
                // remove session
                userWebsocketSessionReactiveRedisTemplate.opsForSet().remove(Const.USER_WEBSOCKET_SESSION_STORE, session)
            }
            .awaitFirstOrNull()
    }

}