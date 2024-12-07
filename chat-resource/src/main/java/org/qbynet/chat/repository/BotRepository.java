package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Bot;
import org.qbynet.chat.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotRepository extends MongoRepository<Bot, String> {
    boolean existsByBot(User bot);
}
