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

package org.cubewhy.qbychat.application.service.mapper.impl

import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.qbychat.application.service.mapper.RoomMapper
import org.cubewhy.qbychat.application.service.mapper.UserMapper
import org.cubewhy.qbychat.domain.model.Room
import org.cubewhy.qbychat.domain.model.RoomType
import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.domain.repository.MemberRepository
import org.cubewhy.qbychat.domain.repository.UserRepository
import org.cubewhy.qbychat.rpc.room.v1.privateRoom
import org.cubewhy.qbychat.rpc.room.v1.room
import org.cubewhy.qbychat.shared.util.protobuf.toFederationId
import org.springframework.stereotype.Service
import org.cubewhy.qbychat.rpc.room.v1.Room as RoomV1

@Service
class RoomMapperImpl(
    private val memberRepository: MemberRepository,
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) : RoomMapper {
    override suspend fun mapToProtobufRoomV1(room: Room, self: User): RoomV1 {
        return room {
            roomId = room.id!!.toFederationId()

            when (room.type) {
                RoomType.PRIVATE -> {
                    // find peer
                    val peer = memberRepository.findAllByRoomIdAndUserIdNot(room.id!!, self.id!!)
                        .flatMap { userRepository.findById(it.userId) }
                        .awaitFirst()

                    privateRoom = privateRoom {
                        peerUser = userMapper.mapToPublicUserProfileV1(peer)
                    }
                }

                RoomType.GROUP -> TODO("map groups")
                RoomType.CHANNEL -> TODO("map channels")
            }
        }
    }
}