package org.qbynet.chat.repository;

import org.qbynet.chat.entity.FavoriteStickerPack;
import org.qbynet.chat.entity.StickerPack;
import org.qbynet.chat.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteStickerPackRepository extends MongoRepository<FavoriteStickerPack, String> {
    List<FavoriteStickerPack> findAllByUser(User user);

    int countByStickerPack(StickerPack stickerPack);

    boolean existsByStickerPackAndUser(StickerPack stickerPack, User user);

    void removeByStickerPackAndUser(StickerPack stickerPack, User user);
}
