/*
 * Copyright (c) 2025. All rights reserved.
 *
 * This file is a part of the QbyChat project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.cubewhy.qbychat.service.impl

import com.google.protobuf.GeneratedMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.avro.FederationMessage
import org.cubewhy.qbychat.entity.*
import org.cubewhy.qbychat.entity.config.InstanceProperties
import org.cubewhy.qbychat.handler.rpc.WebSocketRPCHandler
import org.cubewhy.qbychat.repository.ClientRepository
import org.cubewhy.qbychat.repository.SessionRepository
import org.cubewhy.qbychat.repository.UserRepository
import org.cubewhy.qbychat.service.SessionManager
import org.cubewhy.qbychat.util.Const
import org.cubewhy.qbychat.util.clientMetadata
import org.cubewhy.qbychat.util.protobufEventOf
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.removeAndAwait
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toFlux
import java.time.Instant

@Service
class SessionManagerImpl(
    private val userWebsocketSessionReactiveRedisTemplate: ReactiveRedisTemplate<String, UserWebsocketSession>,
    private val sessionRepository: SessionRepository,
    private val streamBridge: StreamBridge,
    private val userRepository: UserRepository,
    private val instanceProperties: InstanceProperties,
    private val passwordEncoder: PasswordEncoder,
    private val clientRepository: ClientRepository,
) : SessionManager {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Clears "ghost" WebSocket sessions from the active session store.
     *
     * A "ghost" session is defined as a session that:
     * - Belongs to the current instance (based on `instanceProperties.id`), or
     * - Has no associated `instanceId` (i.e., it is orphaned or incomplete).
     *
     * This function runs asynchronously in a coroutine scope and performs the following steps:
     * 1. Scans the Redis store for active WebSocket sessions.
     * 2. Filters out sessions that either belong to the current instance or have no `instanceId`.
     * 3. Logs the removal of each ghost session.
     * 4. Removes each identified ghost session from the Redis store.
     *
     * This method is invoked automatically after the bean is initialized, ensuring any ghost sessions are cleared
     * when the application starts up.
     *
     * @throws Exception If there are any errors during the Redis operations
     */
    @PostConstruct
    private fun clearGhostSessions() {
        CoroutineScope(Dispatchers.Default).launch {
            val ghostSessions =
                userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
                    .filter { it.instanceId == instanceProperties.id || it.instanceId == null }.collectList()
                    .awaitLast()
            ghostSessions.forEach { ghostSession ->
                logger.info { "Remove ghost session ${ghostSession.websocketId}" }
                userWebsocketSessionReactiveRedisTemplate.opsForSet()
                    .removeAndAwait(Const.USER_WEBSOCKET_SESSION_STORE, ghostSession)
            }
        }
    }

    override suspend fun saveWebsocketSession(websocketSession: WebSocketSession, user: User) {
        if (this.isOnSession(websocketSession, user)) {
            logger.warn { "Skipping save session of user ${user.username} on websocket connection ${websocketSession.id} (already exists)" }
            return
        }
        logger.info { "Saving session for ${user.username} at connection ${websocketSession.id}" }
        val wsSessionObject = UserWebsocketSession(
            websocketId = websocketSession.id, userId = user.id!!
        )
        userWebsocketSessionReactiveRedisTemplate.opsForSet().add(Const.USER_WEBSOCKET_SESSION_STORE, wsSessionObject)
            .awaitFirst()
    }

    override suspend fun isOnSession(session: WebSocketSession, user: User): Boolean {
        return findSessions(user).any { it.websocketId == session.id }
    }

    override suspend fun isOnline(userId: String) =
        userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .any { it.userId == userId }.awaitFirstOrNull() ?: false

    override suspend fun isSessionValid(sessionId: String): Boolean {
        return sessionRepository.existsById(sessionId).awaitFirst()
    }

    override fun pushEvent(userId: String, event: GeneratedMessage) {
        // convert to avro
        val payload = FederationMessage.newBuilder().apply {
            this.userId = userId
            this.payload = protobufEventOf(event, userId).toByteString().asReadOnlyByteBuffer()
            this.timestamp = Instant.now().epochSecond
        }
        // send to broker
        if (!streamBridge.send("qbychat-1", payload)) {
            logger.error { "Failed to push event to user $${userId}" }
        }
    }

    override suspend fun processWithSessionLocally(userId: String, func: suspend (WebSocketSession) -> Unit) {
        // check is online
        // avoid query the db if the user is offline
        if (!isOnline(userId)) return
        // find the user
        val user = userRepository.findById(userId).awaitFirst()
        // find all available sessions
        this.findSessions(user).toFlux().mapNotNull {
            // find on local session map
            WebSocketRPCHandler.sessions[it.websocketId]
        }.flatMap { session ->
            mono { func.invoke(session!!) }
        }.awaitLast()
    }

    override suspend fun findSessions(user: User): List<UserWebsocketSession> =
        userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .filter { it.userId == user.id }.collectList().awaitLast()

    override suspend fun getUser(session: WebSocketSession): User? {
        return userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .filter { it.websocketId == session.id }.flatMap {
                // find user
                userRepository.findById(it.userId)
            }.awaitFirstOrNull()
    }

    override suspend fun isAuthorized(session: WebSocketSession): Boolean {
        return userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .any { it.websocketId == session.id }.awaitLast()
    }

    override suspend fun createSession(user: User, session: WebSocketSession): Session {
        val session1 = Session(
            user = user.id!!, clientInfo = session.clientMetadata!!
        )
        return sessionRepository.save(session1).awaitFirst()
    }

    override suspend fun removeWebsocketSession(websocketSession: WebSocketSession) {
        userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .filter { it.websocketId == websocketSession.id }.flatMap { session ->
                // remove session
                userWebsocketSessionReactiveRedisTemplate.opsForSet()
                    .remove(Const.USER_WEBSOCKET_SESSION_STORE, session)
            }.awaitFirstOrNull()
    }
}