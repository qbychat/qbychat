package org.qbynet.chat.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.Sticker;
import org.qbynet.chat.entity.StickerPack;
import org.qbynet.chat.entity.telegram.TelegramFile;
import org.qbynet.chat.entity.telegram.TelegramRestBean;
import org.qbynet.chat.entity.telegram.TelegramStickerSet;
import org.qbynet.chat.service.MediaService;
import org.qbynet.chat.service.StickerService;
import org.qbynet.chat.service.TelegramService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

@Log4j2
@Service
public class TelegramServiceImpl implements TelegramService {
    @Resource
    OkHttpClient okHttpClient;

    @Value("${qbychat.telegram.token}")
    String telegramToken;

    @Resource
    MediaService mediaService;

    @Resource
    StickerService stickerService;

    @Override
    public StickerPack importStickerPack(String name) {
        log.info("Importing sticker pack {} from Telegram", name);
        URI uri = URI.create("https://api.telegram.org/bot" + telegramToken + "/getStickerSet?name=" + name);
        try (Response response = okHttpClient.newCall(new Request.Builder()
            .url(uri.toURL())
            .get()
            .build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            assert response.body() != null;
            TelegramRestBean<TelegramStickerSet> json = new ObjectMapper().readValue(response.body().string(), new TypeReference<>() {
            });
            if (!json.isOk()) {
                throw new RuntimeException(json.getDescription());
            }
            TelegramStickerSet stickerSet = json.getResult();
            log.info("Importing {}", stickerSet.getTitle());
            StickerPack pack = stickerService.createPack(stickerSet);
            stickerSet.getStickers().forEach(s -> {
                if (s.isAnimated() || s.isVideo()) {
                    log.info("Skipping animated sticker pack {} (.tgs or video are not supported)", s);
                }
                this.downloadFile(s.getFileId(), "image/webp", (media) -> {
                    Sticker sticker = stickerService.createSticker(pack, s.getEmoji(), media);
                    log.info("Imported the {} sticker from set {}", sticker.getEmoji(), pack.getTitle());
                });
            });

            return pack;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void downloadFile(String fileId, String contentType, Consumer<Media> consumer) {
        URI uri = URI.create("https://api.telegram.org/bot" + telegramToken + "/getFile?file_id=" + fileId);
        try (Response response = okHttpClient.newCall(new Request.Builder()
            .get()
            .url(uri.toURL())
            .build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            assert response.body() != null;
            TelegramRestBean<TelegramFile> result = new ObjectMapper().readValue(response.body().string(), new TypeReference<>() {
            });
            if (!result.isOk()) {
                throw new RuntimeException(result.getDescription());
            }
            String filePath = result.getResult().getFilePath();
            URI remote = URI.create("https://api.telegram.org/file/bot" + telegramToken + "/" + filePath);
            mediaService.fromRemote(remote, contentType, consumer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
