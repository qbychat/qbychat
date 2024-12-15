package org.qbynet.chat.repository;

import org.qbynet.chat.entity.StickerPack;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StickerPackRepository extends MongoRepository<StickerPack, String> {
}
