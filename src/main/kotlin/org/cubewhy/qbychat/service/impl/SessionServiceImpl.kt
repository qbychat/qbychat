package org.cubewhy.qbychat.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitLast
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.UserWebsocketSession
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.util.Const
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class SessionServiceImpl(
    private val userWebsocketSessionReactiveRedisTemplate: ReactiveRedisTemplate<String, UserWebsocketSession>,
) : SessionService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun saveSession(websocketSession: WebSocketSession, user: User) {
        logger.info { "Saving session for ${user.username} at connection ${websocketSession.id}" }
        val wsSessionObject = UserWebsocketSession(
            websocketId = websocketSession.id,
            userId = user.id!!
        )
        userWebsocketSessionReactiveRedisTemplate.opsForSet().add(Const.USER_WEBSOCKET_SESSION_STORE, wsSessionObject)
            .awaitFirst()
    }

    override suspend fun findSessions(user: User): List<UserWebsocketSession> =
        userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .filter { it.userId == user.id }
            .collectList()
            .awaitLast()

}