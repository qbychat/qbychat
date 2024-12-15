package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.Sticker;
import org.qbynet.chat.entity.StickerPack;
import org.qbynet.chat.entity.TelegramStickerSet;
import org.qbynet.chat.repository.StickerPackRepository;
import org.qbynet.chat.repository.StickerRepository;
import org.qbynet.chat.service.StickerService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
public class StickerServiceImpl implements StickerService {
    @Resource
    StickerRepository stickerRepository;

    @Resource
    StickerPackRepository stickerPackRepository;

    @Override
    public StickerPack create(@NotNull TelegramStickerSet telegramStickerSet) {
        Optional<StickerPack> exist = stickerPackRepository.findByTelegramUpstream(telegramStickerSet.getName());
        if (exist.isPresent()) {
            return exist.get();
        }
        StickerPack stickerPack = new StickerPack();
        stickerPack.setName(telegramStickerSet.getTitle());
        stickerPack.setOwner(null);
        stickerPack.setTelegramUpstream(telegramStickerSet.getName());
        if (stickerPackRepository.existsByLink(telegramStickerSet.getName())) {
            stickerPack.setLink("tg_" + telegramStickerSet.getName());
        } else {
            stickerPack.setLink(telegramStickerSet.getName());
        }
        return stickerPackRepository.save(stickerPack);
    }

    @Override
    public Sticker create(StickerPack pack, String emoji, Media media) {
        Optional<Sticker> exist = stickerRepository.findByPackAndMedia(pack, media);
        Sticker sticker;
        if (exist.isPresent()) {
            sticker = exist.get();
            log.info("Update sticker {} in pack {}", sticker.getAlternativeEmoji(), pack.getName());
        } else {
            sticker = new Sticker();
            sticker.setPack(pack);
            sticker.setAlternativeEmoji(emoji);
        }
        // update media
        sticker.setMedia(media);
        return stickerRepository.save(sticker);
    }
}
