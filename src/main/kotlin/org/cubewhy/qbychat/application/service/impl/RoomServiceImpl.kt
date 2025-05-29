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

package org.cubewhy.qbychat.application.service.impl

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.qbychat.application.service.MemberService
import org.cubewhy.qbychat.application.service.RoomService
import org.cubewhy.qbychat.domain.model.PrivateRoom
import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.domain.repository.RoomRepository
import org.springframework.stereotype.Service

@Service
class RoomServiceImpl(
    private val roomRepository: RoomRepository,
    private val memberService: MemberService
) : RoomService {
    override suspend fun createPrivateRoom(
        user: User,
        peer: User
    ): PrivateRoom {
        val participants = setOf(user.id!!, peer.id!!)
        // check the exist room
        val existsRoom = roomRepository.findByParticipantsExactly(participants).awaitFirstOrNull()
        if (existsRoom != null) return existsRoom

        // create the room
        val room = roomRepository.save(
            PrivateRoom(
                participants = participants
            )
        ).awaitFirst()
        // add members to the room
        memberService.addMembers(room, user, peer)
        return room
    }
}