package org.cubewhy.qbychat.entity

import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Session(
    val id: String? = null,
    val user: String,

    val clientInfo: ClientInfo,
): AuditingEntity()

data class ClientInfo(
    val name: String,
    val version: String,
    val platform: Platform,
) {
    enum class Platform {
        WINDOWS, LINUX, MACOS, ANDROID, IOS, UNKNOWN;
    }
}
