package org.qbynet.chat.service;

import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.Sticker;
import org.qbynet.chat.entity.StickerPack;
import org.qbynet.chat.entity.TelegramStickerSet;

public interface StickerService {
    StickerPack create(TelegramStickerSet telegramStickerSet);

    Sticker create(StickerPack pack, String emoji, Media media);
}
