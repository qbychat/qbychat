package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaRepository extends MongoRepository<Media, String> {
    Optional<Media> findByHashAndName(String hash, String name);

    Page<Media> findAllByNameContainingIgnoreCaseAndContentType(String name, String contentType, Pageable pageable);

    Page<Media> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
