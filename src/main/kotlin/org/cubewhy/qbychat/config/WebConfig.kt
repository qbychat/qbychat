package org.cubewhy.qbychat.config

import org.cubewhy.qbychat.handler.WebsocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping

@Configuration
class WebConfig(
    private val websocketHandler: WebsocketHandler,
) {
    @Bean
    fun handlerMapping(): HandlerMapping {
        val map = mapOf(
            "/ws" to websocketHandler,
        )
        val order = -1

        return SimpleUrlHandlerMapping(map, order)
    }
}