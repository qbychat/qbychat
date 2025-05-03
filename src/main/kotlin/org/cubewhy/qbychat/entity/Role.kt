package org.cubewhy.qbychat.entity

import org.cubewhy.qbychat.websocket.user.v1.Role as ProtoRole

enum class Role {
    USER, ADMIN;

    fun toProtobufType(): ProtoRole {
        return when (this) {
            USER -> ProtoRole.USER
            ADMIN -> ProtoRole.ADMIN
        }
    }
}