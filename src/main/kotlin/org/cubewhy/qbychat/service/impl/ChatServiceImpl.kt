package org.cubewhy.qbychat.service.impl

import com.google.protobuf.ByteString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.qbychat.entity.*
import org.cubewhy.qbychat.repository.ChatRepository
import org.cubewhy.qbychat.service.ChatMapper
import org.cubewhy.qbychat.service.ChatService
import org.cubewhy.qbychat.websocket.chat.WebsocketChat.ChatType
import org.cubewhy.qbychat.websocket.chat.WebsocketChat.CreateGroupRequest
import org.cubewhy.qbychat.websocket.chat.WebsocketChat.CreateGroupResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class ChatServiceImpl(
    private val chatRepository: ChatRepository,
    private val chatMapper: ChatMapper,
) : ChatService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User?
    ): WebsocketResponse {
        return when (method) {
            "CreateGroup" -> this.processCreateGroup(CreateGroupRequest.parseFrom(payload), session, user!!)
            else -> emptyWebsocketResponse()
        }
    }

    suspend fun processCreateGroup(request: CreateGroupRequest, session: WebSocketSession, user: User): WebsocketResponse {
        val chat = Chat(
            title = request.title,
            chatTypeInt = ChatType.GROUP_VALUE,
        )
        // save
        val savedChat = chatRepository.save(chat).awaitFirst()
        logger.info { "Group chat ${savedChat.title} was created." }
        // create event
        val event = chatMapper.buildAddChatEvent(savedChat)
        return websocketResponse(CreateGroupResponse.getDefaultInstance(), event)
    }
}