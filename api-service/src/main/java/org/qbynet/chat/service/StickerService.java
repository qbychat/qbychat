package org.qbynet.chat.service;

import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.Sticker;
import org.qbynet.chat.entity.StickerPack;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.AddStickersDTO;
import org.qbynet.chat.entity.telegram.TelegramStickerSet;

import java.util.List;

public interface StickerService {
    StickerPack createPack(TelegramStickerSet telegramStickerSet);

    StickerPack createPack(String title, String name, User owner);

    Sticker createSticker(StickerPack pack, String emoji, Media media);

    StickerPack findPackById(String id);

    List<Sticker> createStickers(StickerPack pack, List<AddStickersDTO.StickerDTO> stickers);

    List<StickerPack> findFavorites(User user);

    void addFavorite(StickerPack pack, User user);

    void removeFavorite(StickerPack pack, User user);

    StickerPack findPackByName(String name);

    List<Sticker> findStickers(StickerPack pack);

    int countUses(StickerPack pack);

    StickerPack editPack(StickerPack pack, String name, String title);

    Sticker findStickerById(String id);
}
