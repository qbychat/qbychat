package org.cubewhy.qbychat.entity

import org.cubewhy.qbychat.websocket.chat.WebsocketChat.NotificationStatus
import java.time.Instant

data class NotificationPreferences(
    val statusInt: Int,
    val mutedUntil: Instant? = null,
) {
    val status: NotificationStatus
        get() = NotificationStatus.forNumber(this.statusInt)
}