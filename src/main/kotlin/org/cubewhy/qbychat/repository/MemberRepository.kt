package org.cubewhy.qbychat.repository

import org.cubewhy.qbychat.entity.Member
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface MemberRepository : ReactiveMongoRepository<Member, String> {
    fun findAllByUser(user: String): Flux<Member>
    fun findAllByChat(chat: String): Flux<Member>
}