package org.cubewhy.qbychat.repository

import org.cubewhy.qbychat.entity.Chat
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatRepository : ReactiveMongoRepository<Chat, String> {
}