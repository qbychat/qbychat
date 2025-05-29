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

import org.cubewhy.qbychat.application.service.v1.RoomServiceV1
import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.rpc.protocol.v1.RpcRequestMethod
import org.cubewhy.qbychat.rpc.room.v1.CreatePrivateRoomRequest
import org.cubewhy.qbychat.rpc.room.v1.SyncRequest
import org.cubewhy.qbychat.shared.annotations.rpc.RpcMapping
import org.cubewhy.qbychat.shared.model.WebsocketResponse
import org.cubewhy.qbychat.shared.model.websocketResponseOf
import org.springframework.stereotype.Controller

@Controller
class RoomControllerV1(
    private val roomServiceV1: RoomServiceV1
) {
    @RpcMapping(method = RpcRequestMethod.RPC_REQUEST_METHOD_ROOM_SYNC_V1)
    suspend fun sync(request: SyncRequest, user: User): WebsocketResponse {
        return websocketResponseOf(roomServiceV1.sync(request, user))
    }

    @RpcMapping(method = RpcRequestMethod.RPC_REQUEST_METHOD_CREATE_PRIVATE_ROOM_V1)
    suspend fun createPrivateRoom(request: CreatePrivateRoomRequest, user: User): WebsocketResponse {
        return websocketResponseOf(roomServiceV1.createPrivateRoom(request, user))
    }
}