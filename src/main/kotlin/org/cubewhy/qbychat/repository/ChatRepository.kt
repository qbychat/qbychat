package org.cubewhy.qbychat.repository

import org.cubewhy.qbychat.entity.Chat
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ChatRepository : ReactiveMongoRepository<Chat, String> {
    fun existsByName(name: String): Mono<Boolean>
}