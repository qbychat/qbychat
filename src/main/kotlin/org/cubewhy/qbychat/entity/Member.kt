package org.cubewhy.qbychat.entity

import org.cubewhy.qbychat.websocket.chat.WebsocketChat
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Member(
    @Id val id: String? = null,

    val user: String, // user id
    val chat: String, // chat id
    var nickname: String? = null,
    var notificationPreferences: NotificationPreferences = NotificationPreferences(WebsocketChat.NotificationStatus.ALL_VALUE),
    var pinned: Boolean = false, // is this user pinned this chat?
    val owner: Boolean = false,

    val createdAt: Instant = Instant.now(),
)
