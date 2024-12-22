package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Bot;
import org.qbynet.chat.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BotRepository extends MongoRepository<Bot, String> {
    boolean existsByBot(User bot);

    List<Bot> findAllByOwner(User owner);

    Bot findByBot(User user);
}
