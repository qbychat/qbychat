package org.cubewhy.qbychat.service.impl

import org.cubewhy.qbychat.entity.Chat
import org.cubewhy.qbychat.service.ChatMapper
import org.cubewhy.qbychat.websocket.chat.WebsocketChat
import org.springframework.stereotype.Service

@Service
class ChatMapperImpl : ChatMapper {
    override fun toChatVO(chat: Chat): WebsocketChat.Chat = WebsocketChat.Chat.newBuilder().apply {
        chatId = chat.id!!
        name = chat.name
        title = chat.title
        description = chat.description
        type = chat.chatType
    }.build()


    override fun buildAddChatEvent(chat: Chat): WebsocketChat.AddChatEvent =
        WebsocketChat.AddChatEvent.newBuilder().apply {
            this.chat = this@ChatMapperImpl.toChatVO(chat)
        }.build()
}