package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Sticker;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StickerRepository extends MongoRepository<Sticker, String> {
}
