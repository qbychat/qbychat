package org.cubewhy.qbychat.entity

import java.io.Serializable
import java.time.Instant

data class UserSession(
    var userId: String,
    var websocketId: String,

    var clientPublicKey: String,
    var serverPublicKey: String,
    var timestamp: Instant = Instant.now(),
) : Serializable