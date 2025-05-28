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

import org.cubewhy.qbychat.application.service.v1.AuthServiceV1
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection
import org.cubewhy.qbychat.rpc.auth.v1.UsernamePasswordLoginRequest
import org.cubewhy.qbychat.rpc.auth.v1.UsernamePasswordLoginResponse
import org.cubewhy.qbychat.rpc.protocol.v1.RpcRequestMethod
import org.cubewhy.qbychat.shared.annotations.rpc.RpcMapping
import org.cubewhy.qbychat.shared.annotations.rpc.RpcPermissionFlag
import org.springframework.stereotype.Controller

@Controller
class AuthControllerV1(private val authServiceV1: AuthServiceV1) {
    @RpcMapping(
        method = RpcRequestMethod.RPC_REQUEST_METHOD_USERNAME_PASSWORD_LOGIN_V1,
        permissions = RpcPermissionFlag.ALLOW_UNAUTHORIZED_ONLY
    )
    suspend fun usernamePasswordLogin(
        connection: ClientConnection<*>,
        request: UsernamePasswordLoginRequest
    ): UsernamePasswordLoginResponse {
        return authServiceV1.usernamePasswordLogin(connection, request)
    }
}