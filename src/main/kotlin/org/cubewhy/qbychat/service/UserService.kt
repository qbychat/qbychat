package org.cubewhy.qbychat.service

import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.websocket.auth.WebsocketAuth
import org.cubewhy.qbychat.websocket.protocol.Protocol
import org.cubewhy.qbychat.websocket.user.WebsocketUser
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

interface UserService : ReactiveUserDetailsService, PacketProcessor {
    fun loadUserByUsername(username: String): Mono<User>
    suspend fun loadUser(message: Protocol.ServerboundMessage): User?
    suspend fun processRegister(request: WebsocketUser.RegisterRequest, session: WebSocketSession): WebsocketResponse
    suspend fun processUsernamePasswordLogin(
        request: WebsocketAuth.UsernamePasswordLoginRequest,
        session: WebSocketSession
    ): WebsocketResponse

    suspend fun processTokenLogin(
        request: WebsocketAuth.TokenLoginRequest,
        session: WebSocketSession
    ): WebsocketResponse
}