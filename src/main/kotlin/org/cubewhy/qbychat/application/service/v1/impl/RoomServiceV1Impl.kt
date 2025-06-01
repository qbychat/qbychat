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

package org.cubewhy.qbychat.application.service.v1.impl

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.qbychat.application.service.RoomService
import org.cubewhy.qbychat.application.service.mapper.RoomMapper
import org.cubewhy.qbychat.application.service.v1.RoomServiceV1
import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.domain.repository.MemberRepository
import org.cubewhy.qbychat.domain.repository.RoomRepository
import org.cubewhy.qbychat.domain.repository.UserRepository
import org.cubewhy.qbychat.exception.RpcBadRequest
import org.cubewhy.qbychat.rpc.room.v1.*
import org.springframework.stereotype.Service

@Service
class RoomServiceV1Impl(
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
    private val roomMapper: RoomMapper,
    private val roomService: RoomService,
    private val userRepository: UserRepository
) : RoomServiceV1 {
    override suspend fun sync(
        request: SyncRequest,
        user: User
    ): SyncResponse {
        // find rooms
        val joinedRooms = memberRepository.findAllByUserId(user.id!!)
            .flatMap { member -> roomRepository.findById(member.roomId) }
            .collectList()
            .awaitFirst()

        return syncResponse {
            this.joinedRooms.addAll(joinedRooms.map { roomMapper.mapToProtobufRoomV1(it, user) })
        }
    }

    override suspend fun createPrivateRoom(
        request: CreatePrivateRoomRequest,
        user: User
    ): CreatePrivateRoomResponse {
        // TODO federation
        // find peer
        val peer = userRepository.findById(request.peerId.localId.stringId).awaitFirstOrNull()
            ?: throw RpcBadRequest("Peer user not found")
        val room = roomService.createPrivateRoom(user, peer)
        // TODO send message

        // TODO push join room event

        return createPrivateRoomResponse {
            this.room = roomMapper.mapToProtobufRoomV1(room, user)
        }
    }
}