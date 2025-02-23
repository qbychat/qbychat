package org.cubewhy.qbychat.repository

import org.cubewhy.qbychat.entity.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRepository : ReactiveMongoRepository<User, String> {
    fun findByUsername(username: String): Mono<User>
    fun existsByUsernameIgnoreCase(username: String): Mono<Boolean>
}