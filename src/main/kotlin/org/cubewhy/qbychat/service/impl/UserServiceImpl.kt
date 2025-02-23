package org.cubewhy.qbychat.service.impl

import com.google.protobuf.ByteString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.emptyWebsocketResponse
import org.cubewhy.qbychat.entity.websocketResponse
import org.cubewhy.qbychat.repository.UserRepository
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.service.UserService
import org.cubewhy.qbychat.websocket.protocol.Protocol
import org.cubewhy.qbychat.websocket.user.WebsocketUser
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class UserServiceImpl(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val sessionService: SessionService
) : UserService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Value("\${qbychat.user.username.rule.regex}")
    private lateinit var usernameRuleRegexString: String

    @Value("\${qbychat.user.username.rule.description}")
    private lateinit var usernameRuleDescription: String

    override fun findByUsername(username: String): Mono<UserDetails> {
        return userRepository.findByUsername(username)
            .flatMap { user ->
                // build User details
                org.springframework.security.core.userdetails.User.builder()
                    .username(user.username)
                    .password(user.password)
                    .roles(user.role.toString())
                    .build().toMono()
            }
    }

    override fun loadUserByUsername(username: String): Mono<User> {
        return userRepository.findByUsername(username)
    }

    override suspend fun loadUser(message: Protocol.ServerboundMessage): User? {
        if (!message.hasAccount()) return null
        return userRepository.findById(message.account).awaitFirst()
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User?
    ): WebsocketResponse {
        return when (method) {
            "Register" -> this.processRegister(WebsocketUser.RegisterRequest.parseFrom(payload), session)
            else -> emptyWebsocketResponse()
        }
    }

    override suspend fun processRegister(request: WebsocketUser.RegisterRequest, session: WebSocketSession): WebsocketResponse {
        // check username available
        // todo exists chat name
        if (userRepository.existsByUsername(request.username).awaitFirst()) {
            return websocketResponse(WebsocketUser.RegisterResponse.newBuilder().apply {
                this.status = WebsocketUser.RegisterStatus.USERNAME_EXISTS
            }.build())
        }
        // match regex
        if (!Regex(usernameRuleRegexString).matches(request.username)) {
            return websocketResponse(WebsocketUser.RegisterResponse.newBuilder().apply {
                this.status = WebsocketUser.RegisterStatus.BAD_USERNAME
                this.message = usernameRuleDescription
            }.build())
        }
        // create user
        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            nickname = request.username
        )
        // save user
        val savedUser = userRepository.save(user).awaitFirst()
        // add user to session store
        logger.info { "User ${savedUser.username} created" }
        sessionService.saveSession(session, savedUser)
        return websocketResponse(savedUser.id!!, WebsocketUser.RegisterResponse.newBuilder().apply {
            this.status = WebsocketUser.RegisterStatus.SUCCESS
        }.build())
    }
}