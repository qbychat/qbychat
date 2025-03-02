package org.cubewhy.qbychat.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Message(
    @Id val id: String? = null,

    val senderUser: String? = null,
    val senderChat: String? = null,

    val chat: String,
    val content: String,
)
