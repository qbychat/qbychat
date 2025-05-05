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

interface SessionService {
    suspend fun findSessions(user: User): List<UserWebsocketSession>
    suspend fun isAuthorized(session: WebSocketSession): Boolean
    suspend fun saveWebsocketSession(websocketSession: WebSocketSession, user: User)
    suspend fun removeWebsocketSessions(websocketSession: WebSocketSession)
    suspend fun isOnSession(session: WebSocketSession, user: User): Boolean
    suspend fun createSession(user: User, session: WebSocketSession): Session
    suspend fun isSessionValid(sessionId: String): Boolean
    fun pushEvent(userId: String, event: GeneratedMessage)

    suspend fun processWithSessionLocally(userId: String, func: suspend (WebSocketSession) -> Unit)
}