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

import kotlinx.coroutines.reactive.awaitLast
import org.cubewhy.qbychat.application.service.MemberService
import org.cubewhy.qbychat.domain.model.Member
import org.cubewhy.qbychat.domain.model.Room
import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.domain.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberServiceImpl(
    private val memberRepository: MemberRepository
) : MemberService {
    override suspend fun addMembers(
        room: Room,
        vararg users: User
    ) {
        // create members
        memberRepository.saveAll(users.map { user -> Member(roomId = room.id!!, userId = user.id!!) }).awaitLast()
    }
}