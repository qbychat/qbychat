package org.cubewhy.qbychat.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

open class AuditingEntity {
    @CreatedDate
    var createdAt: Instant = Instant.now()

    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
}