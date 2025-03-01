package org.cubewhy.qbychat.service.impl

import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.service.UserMapper
import org.cubewhy.qbychat.util.toProtobufType
import org.cubewhy.qbychat.websocket.user.WebsocketUser
import org.springframework.stereotype.Service

@Service
class UserMapperImpl : UserMapper {
    override fun fullUserVO(user: User): WebsocketUser.User =
        WebsocketUser.User.newBuilder().apply {
            username = user.username
            nickname = user.nickname
            bio = user.bio
            addAllRoles(user.roles.map { it.toProtobufType() })
            createdAt = user.createdAt.toProtobufType()
        }.build()
}