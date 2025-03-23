package org.cubewhy.qbychat.entity

import org.cubewhy.qbychat.websocket.friend.WebsocketFriend
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Friend (
    @Id val id: String,
    val user1: String, // user1, user2 -> user.id
    val user2: String,
    val timestamp: Instant = Instant.now()
){
    fun getTargetId(user: User): String {
        return if (user.id==user1) user2 else user1
    }
}