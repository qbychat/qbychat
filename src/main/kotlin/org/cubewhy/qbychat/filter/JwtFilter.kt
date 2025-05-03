package org.cubewhy.qbychat.filter

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.service.UserService
import org.cubewhy.qbychat.util.JwtUtil
import org.cubewhy.qbychat.util.isValid
import org.cubewhy.qbychat.util.responseFailure
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtFilter(
    private val jwtUtil: JwtUtil,
    private val userService: UserService
) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> = mono {
        val token = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?: return@mono chain.filter(exchange).awaitFirstOrNull()

        val jwt = jwtUtil.resolveJwt(jwtUtil.convertToken(token))
            ?: return@mono exchange.responseFailure(401, "Unauthorized").awaitFirstOrNull()

        val username = (jwt.claims["name"]
            ?: return@mono exchange.responseFailure(401, "Bad Token").awaitFirstOrNull()).asString()

        val isInvalidToken = jwtUtil.isInvalidToken(jwt.id).awaitFirst()
        if (isInvalidToken) {
            return@mono exchange.responseFailure(401, "Unauthorized").awaitFirstOrNull()
        }

        val userDetails = userService.findByUsername(username).awaitFirst()
        if (!jwt.isValid(userDetails.password)) {
            return@mono exchange.responseFailure(401, "Expired session").awaitFirstOrNull()
        }

        val auth: Authentication = UsernamePasswordAuthenticationToken(
            userDetails, userDetails.password, userDetails.authorities
        )
        val securityContext: SecurityContext = SecurityContextImpl(auth)

        return@mono chain.filter(exchange)
            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
            .awaitFirstOrNull()
    }
}