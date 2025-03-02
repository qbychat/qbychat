package org.cubewhy.qbychat.entity

import org.cubewhy.qbychat.websocket.chat.WebsocketChat.NotificationStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class User(
    @Id val id: String? = null,
    var username: String,
    var password: String,
    var roles: List<Role> = listOf(Role.USER),

    var nickname: String,
    var bio: String = "",

    var groupNotificationPreferencesInt: Int = NotificationStatus.ALL_VALUE,
): AuditingEntity() {
    val groupNotificationPreferences: NotificationStatus
        get() = NotificationStatus.forNumber(this.groupNotificationPreferencesInt)
}
