package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.StickerPack;
import org.qbynet.chat.entity.dto.ImportTelegramStickerDTO;
import org.qbynet.chat.entity.vo.StickerPackVO;
import org.qbynet.chat.service.TelegramService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/sticker")
public class StickerController {
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    @Resource
    TelegramService telegramService;

    @PostMapping("tg-import")
    public DeferredResult<ResponseEntity<RestBean<StickerPackVO>>> importStickers(@RequestBody ImportTelegramStickerDTO dto) {
        DeferredResult<ResponseEntity<RestBean<StickerPackVO>>> result = new DeferredResult<>();
        executorService.submit(() -> {
            try {
                StickerPack stickerPack = telegramService.importStickerPack(dto.getName());
                result.setResult(ResponseEntity.ok(RestBean.success(StickerPackVO.from(stickerPack))));
            } catch (Exception e) {
                result.setErrorResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage())));
            }
        });
        return result;
    }
}
