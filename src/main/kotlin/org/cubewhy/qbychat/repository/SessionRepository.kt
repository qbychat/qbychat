package org.cubewhy.qbychat.repository

import org.cubewhy.qbychat.entity.Session
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface SessionRepository : ReactiveMongoRepository<Session, String> {
}