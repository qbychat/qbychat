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

import org.cubewhy.qbychat.application.service.v1.UserServiceV1
import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection
import org.cubewhy.qbychat.shared.annotations.rpc.RpcMapping
import org.cubewhy.qbychat.shared.annotations.rpc.RpcPermissionFlag
import org.cubewhy.qbychat.shared.model.WebsocketResponse
import org.cubewhy.qbychat.shared.model.websocketResponseOf
import org.cubewhy.qbychat.websocket.protocol.v1.RpcRequestMethod
import org.cubewhy.qbychat.websocket.user.v1.RegisterAccountRequest
import org.cubewhy.qbychat.websocket.user.v1.RegisterAccountResponse
import org.cubewhy.qbychat.websocket.user.v1.SyncRequest
import org.springframework.stereotype.Controller

@Controller
class UserControllerV1(private val userServiceV1: UserServiceV1) {
    @RpcMapping(method = RpcRequestMethod.RPC_REQUEST_METHOD_USER_SYNC_V1)
    suspend fun sync(request: SyncRequest, user: User): WebsocketResponse {
        return websocketResponseOf(userServiceV1.sync(request, user))
    }

    @RpcMapping(
        method = RpcRequestMethod.RPC_REQUEST_METHOD_REGISTER_ACCOUNT_V1,
        permissions = RpcPermissionFlag.ALLOW_EXPECT_ANONYMOUS
    )
    suspend fun registerAccount(
        payload: RegisterAccountRequest,
        connection: ClientConnection<*>
    ): RegisterAccountResponse {
        return userServiceV1.registerAccount(payload, connection)
    }
}