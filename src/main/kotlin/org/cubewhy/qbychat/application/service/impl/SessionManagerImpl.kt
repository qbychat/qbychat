/*
 *  Copyright (c) 2025. All rights reserved.
 *  This file is a part of the QbyChat project
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.cubewhy.qbychat.application.service.impl

import com.google.protobuf.GeneratedMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.application.service.SessionManager
import org.cubewhy.qbychat.avro.InstanceMessage
import org.cubewhy.qbychat.config.properties.InstanceProperties
import org.cubewhy.qbychat.domain.model.Session
import org.cubewhy.qbychat.domain.model.SessionMetadata
import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.domain.repository.ClientRepository
import org.cubewhy.qbychat.domain.repository.SessionRepository
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection
import org.cubewhy.qbychat.shared.util.Const
import org.cubewhy.qbychat.shared.util.protobuf.protobufEventOf
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.removeAndAwait
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toFlux
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class SessionManagerImpl(
    private val sessionMetadataReactiveRedisTemplate: ReactiveRedisTemplate<String, SessionMetadata>,
    private val sessionRepository: SessionRepository,
    private val streamBridge: StreamBridge,
    private val instanceProperties: InstanceProperties,
    private val clientRepository: ClientRepository,
    private val environment: Environment,
) : SessionManager {
    companion object {
        private val logger = KotlinLogging.logger {}
        val sessions: ConcurrentHashMap<String, ClientConnection<*>> = ConcurrentHashMap()
    }

    /**
     * Clears "ghost" sessions from the active session store.
     *
     * A "ghost" session is defined as a session that:
     * - Belongs to the current instance (based on `instanceProperties.id`), or
     * - Has no associated `instanceId` (i.e., it is orphaned or incomplete).
     *
     * This function runs asynchronously in a coroutine scope and performs the following steps:
     * 1. Scans the Redis store for active sessions.
     * 2. Filters out sessions that either belong to the current instance or have no `instanceId`.
     * 3. Logs the removal of each ghost session.
     * 4. Removes each identified ghost session from the Redis store.
     *
     * This method is invoked automatically after the bean is initialized, ensuring any ghost sessions are cleared
     * when the application starts up.
     *
     * @throws Exception If there are any errors during the Redis operations
     */
    @EventListener(ApplicationReadyEvent::class)
    private fun clearGhostSessions() {
        if (environment.acceptsProfiles(Profiles.of("test"))) {
            logger.warn { "Skipped clear ghost sessions because test environment is detected" }
            return // skip if running tests
        }

        CoroutineScope(Dispatchers.Default).launch {
            val ghostSessions =
                sessionMetadataReactiveRedisTemplate.opsForSet().scan(Const.SESSION_STORE)
                    .filter { it.instanceId == instanceProperties.id || it.instanceId == null }.collectList()
                    .awaitLast()
            ghostSessions.forEach { ghostSession ->
                logger.info { "Remove ghost session ${ghostSession.sessionId}" }
                sessionMetadataReactiveRedisTemplate.opsForSet()
                    .removeAndAwait(Const.SESSION_STORE, ghostSession)
            }
        }
    }

    override fun addLocalSession(connection: ClientConnection<*>) {
        sessions[connection.id] = connection
    }

    override fun removeLocalSession(id: String) {
        sessions.remove(id)
    }

    override suspend fun isClientOnline(clientId: String): Boolean {
        return sessionMetadataReactiveRedisTemplate.opsForSet().scan(Const.SESSION_STORE)
            .any { it.clientId == clientId }.awaitFirstOrNull() ?: false
    }

    override suspend fun saveSession(connection: ClientConnection<*>, userId: String) {
        if (this.isOnSession(connection, userId)) {
            logger.warn { "Skipping save session of user $userId on connection ${connection.id} (already exists)" }
            return
        }
        logger.debug { "Saving session for $userId at connection ${connection.id}" }
        val sessionObject = SessionMetadata(
            sessionId = connection.id,
            userId = userId,
            clientId = connection.metadata.clientId!!
        )
        sessionMetadataReactiveRedisTemplate.opsForSet().add(Const.SESSION_STORE, sessionObject)
            .awaitFirst()
    }

    override suspend fun isOnSession(connection: ClientConnection<*>, userId: String): Boolean {
        return findAllSessionMetadata(userId).any { it.sessionId == connection.id }
    }

    override suspend fun isOnline(userId: String) =
        sessionMetadataReactiveRedisTemplate.opsForSet().scan(Const.SESSION_STORE)
            .any { it.userId == userId }.awaitFirstOrNull() ?: false

    override suspend fun isSessionValid(sessionId: String): Boolean {
        return sessionRepository.existsById(sessionId).awaitFirst()
    }

    override fun pushEvent(userId: String, event: GeneratedMessage) {
        // convert to avro
        val payload = InstanceMessage.newBuilder().apply {
            this.userId = userId
            this.payload = protobufEventOf(event, userId).toByteString().asReadOnlyByteBuffer()
            this.timestamp = Instant.now().epochSecond
        }
        // send to broker
        if (!streamBridge.send("qbychat-1", payload)) {
            logger.error { "Failed to push event to user $${userId}" }
        }
    }

    override suspend fun processWithSessionLocally(
        userId: String,
        func: suspend (connection: ClientConnection<*>) -> Unit
    ) {
        // find all available sessions
        this.findAllSessionMetadata(userId).toFlux().mapNotNull {
            // find on local session map
            sessions[it.sessionId]
        }.flatMap { session ->
            mono { func.invoke(session!!) }
        }.awaitLast()
    }

    override suspend fun findAllSessionMetadata(userId: String): List<SessionMetadata> =
        sessionMetadataReactiveRedisTemplate.opsForSet().scan(Const.SESSION_STORE)
            .filter { it.userId == userId }.collectList().awaitLast()

    override suspend fun isAuthorized(connection: ClientConnection<*>): Boolean {
        return sessionMetadataReactiveRedisTemplate.opsForSet().scan(Const.SESSION_STORE)
            .any { it.sessionId == connection.id }.awaitLast()
    }

    override suspend fun persistSession(user: User, connection: ClientConnection<*>): Session {
        val session1 = sessionRepository.save(
            Session(
                userId = user.id!!,
                clientId = connection.metadata.clientId!!
            )
        ).awaitFirst()
        logger.info { "Session with user ${user.username} created (client: ${connection.metadata.clientId})" }
        // get client
        val client = clientRepository.findById(connection.metadata.clientId!!).awaitFirst()
        if (client.mainSessionId == null) {
            client.mainSessionId = session1.id
        }
        clientRepository.save(client).awaitFirst()
        this.saveSession(connection, user.id) // add to Redis session store
        return sessionRepository.save(session1).awaitFirst()
    }

    override suspend fun removeSession(connection: ClientConnection<*>) {
        sessionMetadataReactiveRedisTemplate.opsForSet().scan(Const.SESSION_STORE)
            .filter { it.sessionId == connection.id }.flatMap { session ->
                // remove session
                sessionMetadataReactiveRedisTemplate.opsForSet()
                    .remove(Const.SESSION_STORE, session)
            }.awaitFirstOrNull()
    }
}