package org.cubewhy.qbychat.repository

import org.cubewhy.qbychat.entity.Session
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface SessionRepository : ReactiveMongoRepository<Session, String> {
    fun findByIdAndClientInstallationId(id: String, clientInstallationId: String): Mono<Session>
}