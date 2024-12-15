package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.Sticker;
import org.qbynet.chat.entity.StickerPack;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.AddStickersDTO;
import org.qbynet.chat.entity.dto.CreateStickerPackDTO;
import org.qbynet.chat.entity.dto.ImportTelegramStickerDTO;
import org.qbynet.chat.entity.vo.StickerPackVO;
import org.qbynet.chat.entity.vo.StickerVO;
import org.qbynet.chat.service.StickerService;
import org.qbynet.chat.service.TelegramService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/sticker")
public class StickerController {
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Resource
    TelegramService telegramService;

    @Resource
    StickerService stickerService;

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

    @PostMapping("createPack")
    public ResponseEntity<RestBean<StickerPackVO>> createStickerPack(@RequestBody CreateStickerPackDTO dto, @RequestAttribute("user") User user) {
        try {
            StickerPack stickerPack = stickerService.createPack(dto.getTitle(), dto.getName(), user);
            return ResponseEntity.ok(RestBean.success(StickerPackVO.from(stickerPack)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage()));
        }
    }

    @PostMapping("addStickers")
    public ResponseEntity<RestBean<List<StickerVO>>> addStickers(@RequestBody AddStickersDTO dto, @RequestAttribute("user") User user) {
        StickerPack pack = stickerService.findPack(dto.getPack());
        if (!pack.isBelongsTo(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
        }
        List<Sticker> stickers = stickerService.createStickers(pack, dto.getStickers());
        return ResponseEntity.ok(RestBean.success(stickers.stream().map(StickerVO::from).toList()));
    }
}
