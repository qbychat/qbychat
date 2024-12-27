package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Bot;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotRepository extends ReactiveMongoRepository<Bot, String> {
}
