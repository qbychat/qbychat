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

package org.cubewhy.qbychat.interfaces.controller.rpc.v1

import org.cubewhy.qbychat.application.service.v1.SessionServiceV1
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection
import org.cubewhy.qbychat.rpc.protocol.v1.RpcRequestMethod
import org.cubewhy.qbychat.rpc.session.v1.RegisterClientRequest
import org.cubewhy.qbychat.rpc.session.v1.ResumeClientRequest
import org.cubewhy.qbychat.shared.annotations.rpc.RpcMapping
import org.cubewhy.qbychat.shared.annotations.rpc.RpcPermissionFlag
import org.cubewhy.qbychat.shared.model.WebsocketResponse
import org.springframework.stereotype.Controller

@Controller
class ClientControllerV1(
    private val sessionServiceV1: SessionServiceV1,
) {
    @RpcMapping(
        method = RpcRequestMethod.RPC_REQUEST_METHOD_REGISTER_CLIENT_V1,
        permissions = RpcPermissionFlag.ALLOW_ANONYMOUS_ONLY
    )
    suspend fun registerClient(connection: ClientConnection<*>, payload: RegisterClientRequest): WebsocketResponse {
        return sessionServiceV1.registerClient(connection, payload)
    }

    @RpcMapping(
        method = RpcRequestMethod.RPC_REQUEST_METHOD_RESUME_CLIENT_V1,
        permissions = RpcPermissionFlag.ALLOW_ANONYMOUS_ONLY
    )
    suspend fun resumeClient(connection: ClientConnection<*>, payload: ResumeClientRequest): WebsocketResponse {
        return sessionServiceV1.resumeClient(connection, payload)
    }
}