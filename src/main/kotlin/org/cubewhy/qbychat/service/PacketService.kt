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

import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.websocket.protocol.v1.ClientboundHandshake
import org.cubewhy.qbychat.websocket.protocol.v1.ServerboundHandshake
import org.cubewhy.qbychat.websocket.protocol.v1.ServerboundMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType

interface PacketService {
    suspend fun processHandshake(handshake: ServerboundHandshake, session: WebSocketSession): ClientboundHandshake?
    suspend fun process(
        message: ServerboundMessage,
        session: WebSocketSession
    ): WebsocketResponse

    suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User?)
}