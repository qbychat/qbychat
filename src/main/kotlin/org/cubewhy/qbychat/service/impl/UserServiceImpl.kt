package org.cubewhy.qbychat.service.impl

import org.cubewhy.qbychat.entity.User
import org.cubewhy.qbychat.repository.UserRepository
import org.cubewhy.qbychat.service.UserService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class UserServiceImpl(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository
) : UserService {
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
}