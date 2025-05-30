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

import org.cubewhy.qbychat.application.service.mapper.UserMapper
import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.rpc.user.v1.privateUserProfile
import org.cubewhy.qbychat.rpc.user.v1.publicUserProfile
import org.cubewhy.qbychat.shared.util.protobuf.toFederationId
import org.cubewhy.qbychat.shared.util.protobuf.toProtobufTimestamp
import org.springframework.stereotype.Service
import org.cubewhy.qbychat.rpc.user.v1.PrivateUserProfile as PrivateUserProfileV1
import org.cubewhy.qbychat.rpc.user.v1.PublicUserProfile as PublicUserProfileV1

@Service
class UserMapperImpl : UserMapper {
    override fun mapToPublicUserProfileV1(user: User): PublicUserProfileV1 {
        return publicUserProfile {
            userId = user.id!!.toFederationId()
            username = user.username
            nickname = user.nickname
            bio = user.bio
        }
    }

    override fun mapToPrivateUserProfileV1(user: User): PrivateUserProfileV1 {
        return privateUserProfile {
            createTime = user.createdAt.toProtobufTimestamp()
        }
    }
}