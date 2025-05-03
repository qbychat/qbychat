package org.cubewhy.qbychat.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

open class AuditingEntity {
    @CreatedDate
    @Field("created_at")
    var createdAt: Instant = Instant.now()

    @LastModifiedDate
    @Field("updated_at")
    var updatedAt: Instant = Instant.now()
}