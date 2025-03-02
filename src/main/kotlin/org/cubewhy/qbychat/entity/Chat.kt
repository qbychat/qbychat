package org.cubewhy.qbychat.entity

import org.cubewhy.qbychat.websocket.chat.WebsocketChat
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Chat(
    val id: String? = null,

    var name: String? = null, // the name for links
    var title: String,
    var description: String = "",

    val chatTypeInt: Int,
): AuditingEntity() {
    val chatType: WebsocketChat.ChatType
        get() = WebsocketChat.ChatType.forNumber(chatTypeInt)
}
