package org.cubewhy.qbychat.entity.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "instance")
data class InstanceProperties(
    var id: String
)