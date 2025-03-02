package org.cubewhy.qbychat.service.impl

import com.google.protobuf.GeneratedMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.avro.ClusterMessage
import org.cubewhy.qbychat.entity.Session
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.UserWebsocketSession
import org.cubewhy.qbychat.handler.WebsocketHandler
import org.cubewhy.qbychat.repository.SessionRepository
import org.cubewhy.qbychat.repository.UserRepository
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.util.Const
import org.cubewhy.qbychat.util.JwtUtil
import org.cubewhy.qbychat.util.clientInfo
import org.cubewhy.qbychat.util.eventOf
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toFlux
import java.time.Instant

@Service
class SessionServiceImpl(
    private val userWebsocketSessionReactiveRedisTemplate: ReactiveRedisTemplate<String, UserWebsocketSession>,
    private val sessionRepository: SessionRepository,
    private val jwtUtil: JwtUtil,
    private val streamBridge: StreamBridge,
    private val userRepository: UserRepository,
) : SessionService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun saveWebsocketSession(websocketSession: WebSocketSession, user: User) {
        if (this.isOnSession(websocketSession, user)) {
            logger.warn { "Skipping save session of user ${user.username} on websocket connection ${websocketSession.id} (already exists)" }
            return
        }
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

    override suspend fun isSessionValid(sessionId: String): Boolean {
        return sessionRepository.existsById(sessionId).awaitFirst()
    }

    override fun pushEvent(userId: String, event: GeneratedMessage) {
        // convert to avro
        val payload = ClusterMessage.newBuilder().apply {
            this.userId = userId
            this.payload = eventOf(event, userId).toByteString().asReadOnlyByteBuffer()
            this.timestamp = Instant.now().epochSecond
        }
        // send to broker
        if (!streamBridge.send("qbychat-1", payload)) {
            logger.error { "Failed to push event to user $${userId}" }
        }
    }

    override suspend fun processWithSessionLocally(userId: String, func: suspend (WebSocketSession) -> Unit) {
        // find the user
        val user = userRepository.findById(userId).awaitFirst()
        // find all available sessions
        this.findSessions(user).toFlux().mapNotNull {
            // find on local session map
            WebsocketHandler.sessions[it.websocketId]
        }.flatMap { session ->
            mono { func.invoke(session!!) }
        }.awaitLast()
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
            clientInstallationId = session.clientInfo!!.installationId,
            platform = session.clientInfo!!.platform
        )
        return sessionRepository.save(session1).awaitFirst()
    }

    override suspend fun regenerateToken(sessionId: String, webSocketSession: WebSocketSession): String? {
        // find session
        val session =
            sessionRepository.findByIdAndClientInstallationId(sessionId, webSocketSession.clientInfo!!.installationId)
                .awaitFirstOrNull() ?: return null
        // find user
        val user = userRepository.findById(session.user).awaitFirst()
        // regenerate token
        logger.info { "Regenerating token for ${user.username}" }
        return jwtUtil.createJwt(user, session)
    }

    override suspend fun removeWebsocketSessions(websocketSession: WebSocketSession) {
        userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .filter { it.websocketId == websocketSession.id }
            .flatMap { session ->
                // remove session
                userWebsocketSessionReactiveRedisTemplate.opsForSet()
                    .remove(Const.USER_WEBSOCKET_SESSION_STORE, session)
            }
            .awaitFirstOrNull()
    }

}