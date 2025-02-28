package org.cubewhy.qbychat.service

import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.websocket.user.WebsocketUser

interface UserMapper {
    fun fullUserVO(user: User): WebsocketUser.User
}