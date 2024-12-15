package org.qbynet.chat.service;

import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.AddStickersDTO;

import java.util.List;

public interface StickerService {
    StickerPack createPack(TelegramStickerSet telegramStickerSet);

    StickerPack createPack(String title, String name, User owner);

    Sticker createSticker(StickerPack pack, String emoji, Media media);

    StickerPack findPack(String id);

    List<Sticker> createStickers(StickerPack pack, List<AddStickersDTO.StickerDTO> stickers);
}
