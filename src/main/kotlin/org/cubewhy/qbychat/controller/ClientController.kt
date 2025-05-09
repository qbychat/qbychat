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

package org.cubewhy.qbychat.controller

import org.cubewhy.qbychat.annotations.rpc.RPCHandler
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.websocket.protocol.v1.RequestMethod
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.socket.WebSocketSession
import org.cubewhy.qbychat.websocket.session.v1.RegisterClientRequest as RegisterClientRequestV1

@Controller
class ClientController(
    private val sessionService: SessionService,
) {
    @RPCHandler(RequestMethod.REGISTER_CLIENT_V1, allowAnonymous = true)
    suspend fun registerClientV1(session: WebSocketSession, payload: RegisterClientRequestV1): WebsocketResponse {
        return sessionService.registerClient(session, payload)
    }
}