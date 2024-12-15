package org.qbynet.chat.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.qbynet.chat.entity.*;
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
    public void importStickerPack(String name) {
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
            StickerPack pack = stickerService.create(stickerSet);
            stickerSet.getStickers().forEach(s -> this.downloadFile(s.getFileId(), (media) -> {
                Sticker sticker = stickerService.create(pack, s.getEmoji(), media);
                log.info("Imported the {} sticker from set {}", sticker.getAlternativeEmoji(), pack.getName());
            }));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void downloadFile(String fileId, Consumer<Media> consumer) {
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
            mediaService.fromRemote(URI.create("https://api.telegram.org/file/bot" + telegramToken + "/" + filePath), consumer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
