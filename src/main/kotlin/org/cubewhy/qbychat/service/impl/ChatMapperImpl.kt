package org.cubewhy.qbychat.service.impl

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitLast
import org.cubewhy.qbychat.entity.Chat
import org.cubewhy.qbychat.entity.Member
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.repository.ChatRepository
import org.cubewhy.qbychat.repository.MemberRepository
import org.cubewhy.qbychat.repository.UserRepository
import org.cubewhy.qbychat.service.ChatMapper
import org.cubewhy.qbychat.util.toProtobufType
import org.cubewhy.qbychat.websocket.chat.WebsocketChat
import org.cubewhy.qbychat.websocket.chat.WebsocketChat.ChatType
import org.springframework.stereotype.Service

@Service
class ChatMapperImpl(
    private val chatRepository: ChatRepository,
    private val memberRepository: MemberRepository,
    private val userRepository: UserRepository
) : ChatMapper {
    override suspend fun toChatVO(chat: Chat, userId: String): WebsocketChat.Chat {
        val title = if (chat.chatType != ChatType.PRIVATE_MESSAGE) {
            chat.title
        } else {
            // find recipient member
            val anotherMember = memberRepository.findAllByChat(chat.id!!)
                .filter { it.user != userId }
                .awaitLast()
            // find recipient user
            val anotherUser = userRepository.findById(anotherMember.user).awaitFirst()
            anotherUser.nickname // todo use contact name
        }
        return WebsocketChat.Chat.newBuilder().apply {
            chatId = chat.id!!
            chat.name?.let { this.name = it }
            this.title = title
            description = chat.description
            type = chat.chatType
        }.build()
    }

    override suspend fun toChatVO(member: Member): WebsocketChat.Chat {
        // find chat
        val chat = chatRepository.findById(member.chat).awaitFirst()
        return this.toChatVO(chat, member.user).toBuilder().apply {
            this.memberInfo = memberInfoBuilder.apply {
                member.nickname?.let { this.nickname = it }
                pinned = member.pinned
                owner = member.owner
                notificationPreferences = notificationPreferencesBuilder.apply {
                    status = member.notificationPreferences.status
                    member.notificationPreferences.mutedUntil?.let { this.mutedUntil = it.toProtobufType() }
                }.build()
            }.build()
        }.build()
    }


    override suspend fun buildAddChatEvent(chat: Chat, user: User): WebsocketChat.AddChatEvent =
        WebsocketChat.AddChatEvent.newBuilder().apply {
            this.chat = this@ChatMapperImpl.toChatVO(chat, user.id!!)
        }.build()
}