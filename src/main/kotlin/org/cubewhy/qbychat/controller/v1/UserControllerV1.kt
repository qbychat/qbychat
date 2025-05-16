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

package org.cubewhy.qbychat.controller.v1

import org.cubewhy.qbychat.annotations.rpc.RPCMapping
import org.cubewhy.qbychat.annotations.rpc.RPCPermissionFlag
import org.cubewhy.qbychat.service.v1.UserServiceV1
import org.cubewhy.qbychat.websocket.protocol.v1.RequestMethod
import org.cubewhy.qbychat.websocket.user.v1.RegisterAccountRequest
import org.cubewhy.qbychat.websocket.user.v1.RegisterAccountResponse
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.socket.WebSocketSession

@Controller
class UserControllerV1(private val userServiceV1: UserServiceV1) {
    @RPCMapping(RequestMethod.REGISTER_ACCOUNT_V1, permissions = RPCPermissionFlag.ALLOW_EXPECT_ANONYMOUS)
    suspend fun registerAccount(payload: RegisterAccountRequest, session: WebSocketSession): RegisterAccountResponse {
        return userServiceV1.registerAccount(payload, session)
    }
}