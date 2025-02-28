package org.cubewhy.qbychat.entity

import org.cubewhy.qbychat.websocket.user.WebsocketUser

enum class Role {
    USER, ADMIN;

    fun toProtobufType(): WebsocketUser.Role {
        return when (this) {
            USER -> WebsocketUser.Role.USER
            ADMIN -> WebsocketUser.Role.ADMIN
        }
    }
}