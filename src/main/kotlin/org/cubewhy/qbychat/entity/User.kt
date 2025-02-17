package org.cubewhy.qbychat.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class User(
    @Id val id: String? = null,
    var username: String,
    var password: String,
    var role: Role = Role.USER,

    var nickname: String,
    var bio: String,

    val createdAt: Instant = Instant.now(),
)
