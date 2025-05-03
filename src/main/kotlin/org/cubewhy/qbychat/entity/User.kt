package org.cubewhy.qbychat.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
    @Id val id: String? = null,
    var username: String,
    var password: String,
    var roles: List<Role> = listOf(Role.USER),

    var nickname: String,
    var bio: String = "",

    ) : AuditingEntity() {
}
