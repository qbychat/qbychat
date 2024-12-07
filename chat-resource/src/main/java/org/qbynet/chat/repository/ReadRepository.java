package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Read;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadRepository extends MongoRepository<Read, String> {
}
