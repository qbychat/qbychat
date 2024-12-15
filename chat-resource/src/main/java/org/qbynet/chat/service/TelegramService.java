package org.qbynet.chat.service;

import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.StickerPack;

import java.util.function.Consumer;

public interface TelegramService {
    /**
     * Import stickers from Telegram
     *
     * @param name Sticker pack in share link
     */
    StickerPack importStickerPack(String name);

    void downloadFile(String fileId, boolean isLottie, Consumer<Media> consumer);
}
