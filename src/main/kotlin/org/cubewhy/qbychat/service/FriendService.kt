package org.cubewhy.qbychat.service

import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.websocket.friend.WebsocketFriend

interface FriendService : PacketProcessor {
    suspend fun processSyncRequest(
        user: User
    ): WebsocketResponse
}