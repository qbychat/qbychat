package org.qbynet.chat.service;

import org.qbynet.chat.entity.Media;

import java.util.function.Consumer;

public interface TelegramService {
    /**
     * Import stickers from Telegram
     *
     * @param name Sticker pack in share link
     */
    void importStickerPack(String name);

    void downloadFile(String fileId, Consumer<Media> consumer);
}
