package org.cubewhy.qbychat.security

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.exception.WebsocketForbidden
import org.cubewhy.qbychat.exception.WebsocketUnauthorized
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession

@Component
class WebsocketSecurityFilter(
    private val websocketSecurityRules: WebsocketSecurityRules
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun doFilter(service: String, method: String, session: WebSocketSession, user: User?) {
        val key = "$service:$method"
        if (websocketSecurityRules.commonRules[key] == WebsocketSecurityRules.Rule.PERMIT_ALL) {
            return // rule is permitAll, skip check
        }
        if (user == null) {
            logger.info { "Session '$session' is unauthorized and attempted to access '$key'." }
            throw WebsocketUnauthorized()
        }
        // now the user is 100% logged in
        // match roles
        val roleRules = websocketSecurityRules.roleRules[key] ?: return
        if (roleRules.intersect(user.roles.toSet()).isEmpty()) {
            // no rules matched
            logger.info { "User '${user.username}' attempted to access '${key}', but no matching roles were found." }
            throw WebsocketForbidden("Access to '$key' is forbidden for user '${user.username}'.")
        }
    }
}