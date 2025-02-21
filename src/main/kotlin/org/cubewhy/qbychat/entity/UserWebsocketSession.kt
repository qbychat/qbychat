package org.cubewhy.qbychat.entity

import java.io.Serializable
import java.time.Instant

data class UserWebsocketSession(
    var userId: String,
    var websocketId: String,

    var timestamp: Instant = Instant.now(),
) : Serializable