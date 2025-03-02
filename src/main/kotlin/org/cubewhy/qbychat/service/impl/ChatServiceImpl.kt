package org.cubewhy.qbychat.service.impl

import com.google.protobuf.ByteString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.entity.*
import org.cubewhy.qbychat.entity.Chat
import org.cubewhy.qbychat.entity.Member
import org.cubewhy.qbychat.entity.NotificationPreferences
import org.cubewhy.qbychat.repository.ChatRepository
import org.cubewhy.qbychat.repository.MemberRepository
import org.cubewhy.qbychat.service.ChatMapper
import org.cubewhy.qbychat.service.ChatService
import org.cubewhy.qbychat.websocket.chat.WebsocketChat.*
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class ChatServiceImpl(
    private val chatRepository: ChatRepository,
    private val chatMapper: ChatMapper,
    private val memberRepository: MemberRepository,
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
            "Sync" -> this.processSync(SyncRequest.parseFrom(payload), session, user!!)
            "CreateGroup" -> this.processCreateGroup(CreateGroupRequest.parseFrom(payload), session, user!!)
            else -> emptyWebsocketResponse()
        }
    }

    override suspend fun processSync(
        request: SyncRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse {
        // load all members
        val chats = memberRepository.findAllByUser(user.id!!)
            .flatMap { member -> mono { chatMapper.toChatVO(member) } }
            .collectList()
            .awaitLast()
        return websocketResponseOf(SyncResponse.newBuilder().apply {
            addAllChats(chats)
        }.build())
    }

    override suspend fun processCreateGroup(
        request: CreateGroupRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse {
        val chat = Chat(
            title = request.title,
            chatTypeInt = ChatType.GROUP_VALUE,
        )
        // save chat
        val savedChat = chatRepository.save(chat).awaitFirst()
        logger.info { "Group chat ${savedChat.title} was created." }
        // add user to chat
        this.addToChat(user, savedChat, true)
        // create event
        val event = chatMapper.buildAddChatEvent(savedChat, user)
        return websocketResponseOf(CreateGroupResponse.getDefaultInstance(), sharedEventOf(event))
    }

    override suspend fun addToChat(user: User, chat: Chat, owner: Boolean) {
        val member = Member(
            user = user.id!!,
            chat = chat.id!!,
            notificationPreferences = NotificationPreferences(user.groupNotificationPreferencesInt),
            pinned = false,
            owner = owner
        )
        memberRepository.save(member).awaitFirst()
        logger.info { "Added ${user.username} to chat ${chat.title}" }
    }
}