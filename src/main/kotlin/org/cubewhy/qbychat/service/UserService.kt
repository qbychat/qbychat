package org.cubewhy.qbychat.service

import org.cubewhy.qbychat.entity.User
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import reactor.core.publisher.Mono

interface UserService : ReactiveUserDetailsService {
    fun loadUserByUsername(username: String): Mono<User>
}