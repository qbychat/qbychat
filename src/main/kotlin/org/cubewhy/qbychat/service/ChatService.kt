package org.cubewhy.qbychat.service

import org.cubewhy.qbychat.entity.Chat
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.websocket.chat.WebsocketChat
import org.springframework.web.reactive.socket.WebSocketSession

interface ChatService : PacketProcessor {
    suspend fun processCreateGroup(
        request: WebsocketChat.CreateGroupRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse

    suspend fun processSync(
        request: WebsocketChat.SyncRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse

    suspend fun addToChat(user: User, chat: Chat, owner: Boolean = false)
}