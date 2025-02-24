package org.cubewhy.qbychat.entity

import org.cubewhy.qbychat.websocket.protocol.Protocol
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Session(
    val id: String? = null,
    val user: String,
    val clientName: String,
    val clientVersion: String,
    val clientInstallationId: String,
    val platform: Protocol.Platform,
    val timestamp: Instant = Instant.now(),
)

data class ClientInfo(
    val name: String,
    val version: String,
    val installationId: String,
    val platform: Protocol.Platform,
)
