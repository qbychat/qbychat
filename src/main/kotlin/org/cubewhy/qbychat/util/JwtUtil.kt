package org.cubewhy.qbychat.util

import cn.hutool.crypto.SecureUtil
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import org.cubewhy.qbychat.entity.Session
import org.cubewhy.qbychat.entity.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Component
class JwtUtil(
    private val stringReactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
) {
    @Value("\${spring.security.jwt.key}")
    private lateinit var key: String

    @Value("\${spring.security.jwt.expire}")
    private var expire = 0

    fun resolveBearerJwt(token: String?): DecodedJWT? {
        val token1 = convertToken(token) ?: return null
        return resolveJwt(token1)
    }

    fun resolveJwt(token: String?, ignoreExpired: Boolean = false): DecodedJWT? {
        if (token == null) {
            return null // incorrect token
        }
        val algorithm: Algorithm = Algorithm.HMAC256(key)
        val jwtVerifier: JWTVerifier = JWT.require(algorithm).build()
        try {
            val jwt: DecodedJWT = jwtVerifier.verify(token)
            if (ignoreExpired) return jwt
            val expireAt: Date = jwt.expiresAt
            return if (Date().after(expireAt)) null else jwt
        } catch (error: JWTVerificationException) {
            // User modified this
            return null
        }
    }

    fun createJwt(user: User): String {
        val algorithm: Algorithm = Algorithm.HMAC256(key)
        return JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withClaim("id", user.id) // internal id
            .withClaim("username", user.username)
            .withClaim("pwd-hash", SecureUtil.sha1(user.password))
            .withClaim("roles", user.roles.map { it.name })
            .withExpiresAt(expireDate) // now + {date}
            .withIssuedAt(Date()) // time now
            .sign(algorithm)
    }

    fun createJwt(user: User, session: Session): String {
        val algorithm: Algorithm = Algorithm.HMAC256(key)
        return JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withClaim("id", user.id) // user id
            .withClaim("session", session.id) // session id
            .withClaim("username", user.username)
            .withClaim("roles", user.roles.map { it.name })
            .withExpiresAt(expireDate) // now + {date}
            .withIssuedAt(Date()) // time now
            .sign(algorithm)
    }

    private val expireDate: Date
        get() {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR, 24 * expire)
            return calendar.time
        }

    fun convertToken(headerToken: String?): String? {
        if (headerToken == null) {
            return null // incorrect token
        }
        if (!headerToken.startsWith("Bearer ")) return headerToken
        // cut "Bearer "
        return headerToken.substring(7)
    }

    fun isInvalidToken(tokenId: String): Mono<Boolean> {
        return stringReactiveRedisTemplate.hasKey(Const.EXPIRED_TOKEN + tokenId)
    }

    fun expireToken(token: String): Mono<Boolean> {
        val parsedToken = convertToken(token) ?: return false.toMono()
        // parse token
        val jwt = this.resolveJwt(parsedToken) ?: return false.toMono()
        // expire token
        return stringReactiveRedisTemplate.opsForValue().set(Const.EXPIRED_TOKEN + jwt.id, "0").then(Mono.just(true))
    }
}

fun DecodedJWT.isValid(password: String?): Boolean {
    // verify the password hash
    val claim = this.getClaim("pwd-hash")
    if (claim.isNull || claim.isMissing) return false
    if (password == null) return true // this user does not have a password, skip checking
    return claim.asString() == SecureUtil.sha1(password)
}