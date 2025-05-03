package org.cubewhy.qbychat.entity.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "qbychat")
data class QbyChatProperties(
    var websocket: WebsocketProperties
) {
    data class WebsocketProperties(
        var path: String
    )
}