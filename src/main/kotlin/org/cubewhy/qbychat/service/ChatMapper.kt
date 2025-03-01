package org.cubewhy.qbychat.service

import org.cubewhy.qbychat.entity.Chat
import org.cubewhy.qbychat.websocket.chat.WebsocketChat

interface ChatMapper {
    fun buildAddChatEvent(chat: Chat): WebsocketChat.AddChatEvent
    fun toChatVO(chat: Chat): WebsocketChat.Chat
}