package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.Sticker;
import org.qbynet.chat.entity.StickerPack;
import org.qbynet.chat.entity.TelegramStickerSet;
import org.qbynet.chat.repository.StickerPackRepository;
import org.qbynet.chat.repository.StickerRepository;
import org.qbynet.chat.service.StickerService;
import org.springframework.stereotype.Service;

@Service
public class StickerServiceImpl implements StickerService {
    @Resource
    StickerRepository stickerRepository;

    @Resource
    StickerPackRepository stickerPackRepository;

    @Override
    public StickerPack create(@NotNull TelegramStickerSet telegramStickerSet) {
        StickerPack stickerPack = new StickerPack();
        stickerPack.setName(telegramStickerSet.getTitle());
        stickerPack.setOwner(null);
        stickerPack.setLink(telegramStickerSet.getName());
        return stickerPackRepository.save(stickerPack);
    }

    @Override
    public Sticker create(StickerPack pack, String emoji, Media media) {
        Sticker sticker = new Sticker();
        sticker.setPack(pack);
        sticker.setAlternativeEmoji(emoji);
        sticker.setMedia(media);
        return stickerRepository.save(sticker);
    }
}
