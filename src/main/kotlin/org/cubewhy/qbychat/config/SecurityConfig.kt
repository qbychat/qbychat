/*
 * Copyright (c) 2025. All rights reserved.
 *
 * This file is a part of the QbyChat project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.cubewhy.qbychat.config

import org.cubewhy.qbychat.entity.vo.AuthorizeVO
import org.cubewhy.qbychat.filter.JwtFilter
import org.cubewhy.qbychat.service.UserService
import org.cubewhy.qbychat.util.JwtUtil
import org.cubewhy.qbychat.util.responseFailure
import org.cubewhy.qbychat.util.responseSuccess
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtUtil: JwtUtil,
    private val jwtFilter: JwtFilter,
    private val userService: UserService
) {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            authorizeExchange {
                authorize(
                    pathMatchers("/ws", "/.well-known/**", "/dashboard/**", "/dashboard"), permitAll
                )
                authorize(
                    pathMatchers("/api/**"), authenticated
                )
                authorize(anyExchange, permitAll)
            }
            formLogin {
                loginPage = "/api/user/login"
                authenticationSuccessHandler = AuthSuccessHandler(jwtUtil, userService)
                authenticationFailureHandler = AuthFailureHandler
            }
            addFilterBefore(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            logout {
                logoutUrl = "/api/user/logout"
                logoutSuccessHandler = LogoutSuccessHandler(jwtUtil = jwtUtil)
            }
            exceptionHandling {
                authenticationEntryPoint = UnauthorizedHandler
                accessDeniedHandler = AccessDenyHandler
            }
            csrf {
                disable()
            }
            cors {
                disable()
            }
            httpBasic { }
        }
    }

    class AuthSuccessHandler(private val jwtUtil: JwtUtil, private val userService: UserService) :
        ServerAuthenticationSuccessHandler {
        override fun onAuthenticationSuccess(
            webFilterExchange: WebFilterExchange,
            authentication: Authentication
        ): Mono<Void> {
            // generate JWT
            val details = authentication.principal as User
            // find web user
            return userService.loadUserByUsername(details.username).flatMap { user ->
                val jwt = jwtUtil.createJwt(user)
                // parse jwt
                val parsedJwt = jwtUtil.resolveJwt(jwt)!!
                AuthorizeVO(user.username, jwt, parsedJwt.expiresAt.time, user.roles.map { it.name }).toMono()
            }.flatMap { vo ->
                webFilterExchange.exchange.responseSuccess(vo)
            }
        }
    }

    object AuthFailureHandler : ServerAuthenticationFailureHandler {
        override fun onAuthenticationFailure(
            webFilterExchange: WebFilterExchange,
            exception: AuthenticationException
        ): Mono<Void> {
            return webFilterExchange.exchange.responseFailure(401, exception.message!!)
        }
    }

    class LogoutSuccessHandler(private val jwtUtil: JwtUtil) : ServerLogoutSuccessHandler {
        override fun onLogoutSuccess(webFilterExchange: WebFilterExchange, authentication: Authentication): Mono<Void> {
            val token = webFilterExchange.exchange.request.headers[HttpHeaders.AUTHORIZATION]?.get(0)
            if (token.isNullOrEmpty()) {
                return webFilterExchange.exchange.responseFailure(400, "Bad Request")
            }
            // expire token
            return jwtUtil.expireToken(token).flatMap { result ->
                return@flatMap if (result) {
                    webFilterExchange.exchange.responseSuccess(null) // success
                } else {
                    webFilterExchange.exchange.responseFailure(400, "Bad Request") // bad token
                }
            }
        }
    }

    object UnauthorizedHandler : ServerAuthenticationEntryPoint {
        override fun commence(exchange: ServerWebExchange, ex: AuthenticationException): Mono<Void> {
            return exchange.responseFailure(401, ex.message!!)
        }
    }

    object AccessDenyHandler : ServerAccessDeniedHandler {
        override fun handle(exchange: ServerWebExchange, denied: AccessDeniedException): Mono<Void> {
            return exchange.responseFailure(403, denied.message!!)
        }
    }
}