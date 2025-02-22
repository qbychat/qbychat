package org.cubewhy.qbychat.config

import org.cubewhy.qbychat.handler.WebsocketHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping

@Configuration
class WebConfig(
    private val websocketHandler: WebsocketHandler,
) {
    @Value("\${qbychat.websocket.path}")
    private lateinit var websocketPath: String
    
    @Bean
    fun handlerMapping(): HandlerMapping {
        val map = mapOf(
            websocketPath to websocketHandler,
        )
        val order = -1

        return SimpleUrlHandlerMapping(map, order)
    }
}