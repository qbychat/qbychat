package org.cubewhy.qbychat.util.security

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.qbychat.exception.WebsocketUnauthorized
import org.cubewhy.qbychat.service.SessionService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession

@Component
class WebsocketSecurityFilter(
    private val sessionService: SessionService,
    private val websocketSecurityRules: WebsocketSecurityRules
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun doFilter(service: String, method: String, session: WebSocketSession) {
        val key = "$service:$method"
        if (!websocketSecurityRules.rules.containsKey(key) && !sessionService.isAuthorized(session)) {
            logger.info { "Session $session unauthorized but tried to request $key" }
            throw WebsocketUnauthorized()
        }

    }
}