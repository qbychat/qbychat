package org.qbynet.chat.repository;

import org.qbynet.chat.entity.LinkPreview;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkPreviewRepository extends MongoRepository<LinkPreview, String> {

    Optional<LinkPreview> findByLink(String link);
}
