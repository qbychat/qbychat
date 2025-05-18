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

package org.cubewhy.qbychat.application.service

import com.google.protobuf.GeneratedMessage
import org.cubewhy.qbychat.domain.model.Session
import org.cubewhy.qbychat.domain.model.SessionMetadata
import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection

interface SessionManager {
    fun addLocalSession(connection: ClientConnection<*>)
    fun removeLocalSession(id: String)

    /**
     * Retrieves a list of active WebSocket sessions for a given user.
     *
     * This function scans the Redis set containing active sessions, filters them by the user's ID,
     * and returns a list of WebSocket sessions associated with the user.
     *
     * @param user The user whose sessions are to be retrieved
     * @return A list of [SessionMetadata] objects associated with the given user
     */
    suspend fun findAllSessionMetadata(user: User): List<SessionMetadata>

    /**
     * Checks if the given WebSocket session is authorized (i.e., has a corresponding user session).
     *
     * This function checks whether the WebSocket session exists in the Redis store of active sessions.
     *
     * @param connection The WebSocket session to check authorization for
     * @return `true` if the session is authorized (exists in Redis), otherwise `false`
     */
    suspend fun isAuthorized(connection: ClientConnection<*>): Boolean
    suspend fun saveSession(connection: ClientConnection<*>, user: User)

    /**
     * Removes a WebSocket session from the active sessions store.
     *
     * This function scans the Redis store for the given WebSocket session and removes it from the active sessions set.
     * It ensures the session is no longer tracked.
     *
     * @param connection The WebSocket session to be removed from the store
     */
    suspend fun removeSession(connection: ClientConnection<*>)

    suspend fun isOnSession(connection: ClientConnection<*>, user: User): Boolean

    /**
     * Creates a new session for a given user and WebSocket session.
     *
     * This function creates a new session entry by associating the given user and WebSocket session metadata.
     * The session is then saved to the session repository.
     *
     * @param user The user who is creating the session
     * @param connection The session that is being created
     * @return The created [Session] object
     */
    suspend fun createSession(user: User, connection: ClientConnection<*>): Session
    suspend fun isSessionValid(sessionId: String): Boolean
    suspend fun isOnline(userId: String): Boolean

    suspend fun isClientOnline(clientId: String): Boolean

    fun pushEvent(userId: String, event: GeneratedMessage)
    suspend fun processWithSessionLocally(userId: String, func: suspend (ClientConnection<*>) -> Unit)
}