package org.cubewhy.qbychat.service.impl

import com.google.protobuf.ByteString
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.emptyWebsocketResponse
import org.cubewhy.qbychat.repository.ChatRepository
import org.cubewhy.qbychat.service.ChatService
import org.cubewhy.qbychat.websocket.chat.WebsocketChat.CreateChatRequest
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class ChatServiceImpl(
    private val chatRepository: ChatRepository,
) : ChatService {
    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User?
    ): WebsocketResponse {
        return when (method) {
            "CreateChat" -> this.processCreateChat(CreateChatRequest.parseFrom(payload), session, user!!)
            else -> emptyWebsocketResponse()
        }
    }

    suspend fun processCreateChat(request: CreateChatRequest, session: WebSocketSession, user: User): WebsocketResponse {
        TODO("create chat")
    }
}