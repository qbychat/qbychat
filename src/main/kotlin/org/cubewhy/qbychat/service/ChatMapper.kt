package org.cubewhy.qbychat.service

import org.cubewhy.qbychat.entity.Chat
import org.cubewhy.qbychat.entity.Member
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.websocket.chat.WebsocketChat

interface ChatMapper {
    suspend fun buildAddChatEvent(chat: Chat, user: User): WebsocketChat.AddChatEvent
    suspend fun toChatVO(chat: Chat, userId: String): WebsocketChat.Chat
    suspend fun toChatVO(member: Member): WebsocketChat.Chat
}