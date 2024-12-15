package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.AddStickersDTO;
import org.qbynet.chat.repository.StickerPackRepository;
import org.qbynet.chat.repository.StickerRepository;
import org.qbynet.chat.service.MediaService;
import org.qbynet.chat.service.StickerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Log4j2
@Service
public class StickerServiceImpl implements StickerService {
    @Resource
    MediaService mediaService;

    @Resource
    StickerRepository stickerRepository;

    @Resource
    StickerPackRepository stickerPackRepository;

    @Override
    public StickerPack createPack(@NotNull TelegramStickerSet telegramStickerSet) {
        Optional<StickerPack> exist = stickerPackRepository.findByTelegramUpstream(telegramStickerSet.getName());
        if (exist.isPresent()) {
            return exist.get();
        }
        StickerPack stickerPack = new StickerPack();
        stickerPack.setTitle(telegramStickerSet.getTitle());
        stickerPack.setOwner(null);
        stickerPack.setTelegramUpstream(telegramStickerSet.getName());
        if (stickerPackRepository.existsByName(telegramStickerSet.getName())) {
            stickerPack.setName("tg_" + telegramStickerSet.getName());
        } else {
            stickerPack.setName(telegramStickerSet.getName());
        }
        return stickerPackRepository.save(stickerPack);
    }

    @Override
    public StickerPack createPack(String title, String name, User owner) {
        if (stickerPackRepository.existsByName(name)) {
            throw new IllegalArgumentException("Name already exists");
        }
        if (name.startsWith("tg_")) {
            throw new IllegalArgumentException("Name must not start with 'tg_'");
        }
        if (!Pattern.matches("^[a-zA-Z0-9_]+$", name)) {
            throw new IllegalArgumentException("Bad name");
        }

        StickerPack stickerPack = new StickerPack();
        stickerPack.setTitle(title);
        stickerPack.setName(name);
        stickerPack.setOwner(owner);
        return stickerPackRepository.save(stickerPack);
    }

    @Override
    public Sticker createSticker(StickerPack pack, String emoji, Media media) {
        Optional<Sticker> exist = stickerRepository.findByPackAndMedia(pack, media);
        Sticker sticker;
        if (exist.isPresent()) {
            sticker = exist.get();
            log.info("Update sticker {} in pack {}", sticker.getEmoji(), pack.getTitle());
        } else {
            sticker = new Sticker();
            sticker.setPack(pack);
            sticker.setEmoji(emoji);
        }
        // update media
        sticker.setMedia(media);
        return stickerRepository.save(sticker);
    }

    @Override
    public StickerPack findPack(String id) {
        return stickerPackRepository.findById(id).orElse(null);
    }

    @Override
    public List<Sticker> createStickers(StickerPack pack, @NotNull List<AddStickersDTO.StickerDTO> stickers) {
        return stickerRepository.saveAll(stickers.stream().map(dto -> {
            Media media = mediaService.findById(dto.getMedia());
            if (media == null) return null;
            Sticker sticker = new Sticker();
            sticker.setPack(pack);
            sticker.setEmoji(dto.getEmoji());
            sticker.setMedia(media);
            return sticker;
        }).filter(Objects::nonNull).toList());
    }
}
