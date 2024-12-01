package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Media;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaRepository extends MongoRepository<Media, String> {
    Optional<Media> findByHash(String hash);
}
