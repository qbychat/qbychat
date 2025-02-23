package org.cubewhy.qbychat.service.impl

import com.google.protobuf.ByteString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.emptyWebsocketResponse
import org.cubewhy.qbychat.entity.websocketResponse
import org.cubewhy.qbychat.repository.UserRepository
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.service.UserService
import org.cubewhy.qbychat.util.JwtUtil
import org.cubewhy.qbychat.util.toProtobufType
import org.cubewhy.qbychat.websocket.auth.WebsocketAuth
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
    private val sessionService: SessionService,
    private val jwtUtil: JwtUtil
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
            "UsernamePasswordLogin" -> this.processUsernamePasswordLogin(WebsocketAuth.UsernamePasswordLoginRequest.parseFrom(payload), session)
            "Register" -> this.processRegister(WebsocketUser.RegisterRequest.parseFrom(payload), session)
            else -> emptyWebsocketResponse()
        }
    }

    override suspend fun processRegister(request: WebsocketUser.RegisterRequest, session: WebSocketSession): WebsocketResponse {
        // check username available
        // todo exists chat name
        if (userRepository.existsByUsernameIgnoreCase(request.username).awaitFirst()) {
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
        sessionService.saveWebsocketSession(session, savedUser)
        // create qbychat session
        val sessionInfo = sessionService.createSession(savedUser, session)
        // create token from sessionInfo
        val jwt = jwtUtil.createJwt(savedUser, sessionInfo)
        return websocketResponse(savedUser.id!!, WebsocketUser.RegisterResponse.newBuilder().apply {
            this.status = WebsocketUser.RegisterStatus.SUCCESS
        }.build(), this.buildTokenUpdateEvent(jwt))
    }

    override suspend fun processUsernamePasswordLogin(request: WebsocketAuth.UsernamePasswordLoginRequest, session: WebSocketSession): WebsocketResponse {
        // find user by username
        val user = userRepository.findByUsername(request.username).awaitFirstOrNull()
            ?: return websocketResponse(WebsocketAuth.UsernamePasswordLoginResponse.newBuilder().apply {
                status = WebsocketAuth.LoginStatus.USER_NOT_FOUND
            }.build())
        // assert password
        if (!passwordEncoder.matches(request.password, user.password)) {
            return websocketResponse(WebsocketAuth.UsernamePasswordLoginResponse.newBuilder().apply {
                status = WebsocketAuth.LoginStatus.BAD_PASSWORD
            }.build())
        }
        // create a session
        sessionService.saveWebsocketSession(session, user)
        val sessionInfo = sessionService.createSession(user, session)
        logger.info { "User ${user.username} logged in" }
        // generate jwt
        val jwt = jwtUtil.createJwt(user, sessionInfo)
        return websocketResponse(user.id!!, WebsocketAuth.UsernamePasswordLoginResponse.newBuilder().apply {
            status = WebsocketAuth.LoginStatus.SUCCESS
        }.build(), this.buildTokenUpdateEvent(jwt))
    }

    private fun buildTokenUpdateEvent(jwt: String): WebsocketAuth.TokenUpdateEvent {
        // parse jwt and get expire date
        val jwtExpireAt = jwtUtil.resolveJwt(jwt)!!.expiresAt
        return WebsocketAuth.TokenUpdateEvent.newBuilder().apply {
            token = jwt
            expireAt = jwtExpireAt.toProtobufType()
        }.build()
    }
}