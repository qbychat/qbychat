package org.cubewhy.qbychat.repository

import org.cubewhy.qbychat.entity.Friend
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FriendRepository : ReactiveMongoRepository<Friend, String> {
    @Query("{ \$or: [ { 'user1': ?0 }, { 'user2': ?0 } ] }")
    fun findFriendRelations(user: String): Flux<Friend>

    @Query("{ \$or: [ { 'user1': ?0, 'user2': ?1 }, { 'user1': ?1, 'user2': ?0 } ] }")
    fun findFriendRelation(user: String, target: String): Mono<Friend>

    //    @Query("{ \$or: [{'user1': ?0, 'user2': ?1}, {'user1': ?1, 'user2': ?0}] }")
    fun countByUser1(user1: String): Mono<Long>
}