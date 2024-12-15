package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.Sticker;
import org.qbynet.chat.entity.StickerPack;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StickerRepository extends MongoRepository<Sticker, String> {
    Optional<Sticker> findByPackAndAlternativeEmoji(StickerPack pack, String alternativeEmoji);

    Optional<Sticker> findByPackAndMedia(StickerPack pack, Media media);
}
