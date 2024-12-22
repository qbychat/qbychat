package org.qbynet.chat.repository;

import org.qbynet.chat.entity.StickerPack;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StickerPackRepository extends MongoRepository<StickerPack, String> {
    boolean existsByName(String link);

    Optional<StickerPack> findByTelegramUpstream(String telegramUpstream);

    Optional<StickerPack> findByName(String name);
}
