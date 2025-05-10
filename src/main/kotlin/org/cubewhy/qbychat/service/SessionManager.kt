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

package org.cubewhy.qbychat.service

import com.google.protobuf.GeneratedMessage
import org.cubewhy.qbychat.entity.Session
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.UserWebsocketSession
import org.springframework.web.reactive.socket.WebSocketSession

interface SessionManager {
    /**
     * Retrieves a list of active WebSocket sessions for a given user.
     *
     * This function scans the Redis set containing active sessions, filters them by the user's ID,
     * and returns a list of WebSocket sessions associated with the user.
     *
     * @param user The user whose sessions are to be retrieved
     * @return A list of [UserWebsocketSession] objects associated with the given user
     */
    suspend fun findSessions(user: User): List<UserWebsocketSession>

    /**
     * Retrieves the user associated with a given WebSocket session.
     *
     * This function searches for the WebSocket session in Redis, and if found, fetches the associated user
     * from the user repository.
     *
     * @param session The WebSocket session whose associated user is to be fetched
     * @return The [User] associated with the given WebSocket session, or null if no user is found
     */
    suspend fun getUser(session: WebSocketSession): User?

    /**
     * Checks if the given WebSocket session is authorized (i.e., has a corresponding user session).
     *
     * This function checks whether the WebSocket session exists in the Redis store of active sessions.
     *
     * @param session The WebSocket session to check authorization for
     * @return `true` if the session is authorized (exists in Redis), otherwise `false`
     */
    suspend fun isAuthorized(session: WebSocketSession): Boolean
    suspend fun saveWebsocketSession(websocketSession: WebSocketSession, user: User)

    /**
     * Removes a WebSocket session from the active sessions store.
     *
     * This function scans the Redis store for the given WebSocket session and removes it from the active sessions set.
     * It ensures the session is no longer tracked.
     *
     * @param websocketSession The WebSocket session to be removed from the store
     */
    suspend fun removeWebsocketSession(websocketSession: WebSocketSession)
    suspend fun isOnSession(session: WebSocketSession, user: User): Boolean

    /**
     * Creates a new session for a given user and WebSocket session.
     *
     * This function creates a new session entry by associating the given user and WebSocket session metadata.
     * The session is then saved to the session repository.
     *
     * @param user The user who is creating the session
     * @param session The WebSocket session that is being created
     * @return The created [Session] object
     */
    suspend fun createSession(user: User, session: WebSocketSession): Session
    suspend fun isSessionValid(sessionId: String): Boolean
    suspend fun isOnline(userId: String): Boolean

    fun pushEvent(userId: String, event: GeneratedMessage)
    suspend fun processWithSessionLocally(userId: String, func: suspend (WebSocketSession) -> Unit)
}