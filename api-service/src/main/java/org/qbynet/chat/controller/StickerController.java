package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.qbynet.chat.entity.Sticker;
import org.qbynet.chat.entity.StickerPack;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.*;
import org.qbynet.chat.entity.vo.StickerPackVO;
import org.qbynet.chat.entity.vo.StickerVO;
import org.qbynet.chat.service.StickerService;
import org.qbynet.chat.service.TelegramService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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

    @Value("${qbychat.telegram.enabled}")
    boolean telegramEnabled;

    @GetMapping("info")
    public ResponseEntity<RestBean<StickerVO>> info(@RequestParam String id) {
        Sticker sticker = stickerService.findStickerById(id);
        if (sticker == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "No sticker with id " + id));
        }
        return ResponseEntity.ok(RestBean.success(StickerVO.from(sticker)));
    }

    @GetMapping("raw")
    public ResponseEntity<RestBean<List<StickerVO>>> raw(@RequestParam String id, HttpServletResponse response) throws Exception {
        Sticker sticker = stickerService.findStickerById(id);
        if (sticker == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "No sticker with id " + id));
        }
        response.sendRedirect("/api/media/" + sticker.getMedia().getId() + "/raw");
        return null;
    }

    @GetMapping("pack/info")
    public ResponseEntity<RestBean<StickerPackVO>> packInfo(@RequestParam(required = false) String id, @RequestParam(required = false) String name) {
        StickerPack pack = null;
        if (id != null) {
            pack = stickerService.findPackById(id);
        } else if (name != null) {
            pack = stickerService.findPackByName(name);
        }
        if (pack == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "Sticker pack not found"));
        }
        return ResponseEntity.ok(RestBean.success(StickerPackVO.from(pack)
            .stickers(stickerService.findStickers(pack).stream().map(Sticker::getId).toList())
            .uses(stickerService.countUses(pack))
            .build()
        ));
    }

    @PostMapping("tg-import")
    @Secured("SCOPE_sticker.manage")
    public DeferredResult<ResponseEntity<RestBean<StickerPackVO>>> importStickers(@RequestBody ImportTelegramStickerDTO dto) {
        DeferredResult<ResponseEntity<RestBean<StickerPackVO>>> result = new DeferredResult<>();
        if (!telegramEnabled) {
            result.setErrorResult(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(RestBean.failure(503, "Telegram feature is not enabled")));
            return result;
        }
        executorService.submit(() -> {
            try {
                StickerPack stickerPack = telegramService.importStickerPack(dto.getName());
                result.setResult(ResponseEntity.ok(RestBean.success(StickerPackVO.from(stickerPack).build())));
            } catch (Exception e) {
                result.setErrorResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage())));
            }
        });
        return result;
    }

    @PostMapping("createPack")
    @Secured("SCOPE_sticker.manage")
    public ResponseEntity<RestBean<StickerPackVO>> createStickerPack(@RequestBody CreateStickerPackDTO dto, @RequestAttribute("user") User user) {
        try {
            StickerPack stickerPack = stickerService.createPack(dto.getTitle(), dto.getName(), user);
            return ResponseEntity.ok(RestBean.success(StickerPackVO.from(stickerPack).build()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage()));
        }
    }

    @PostMapping("editPack")
    @Secured("SCOPE_sticker.manage")
    public ResponseEntity<RestBean<StickerPackVO>> editStickerPack(@RequestBody EditStickerPackDTO dto, @RequestAttribute("user") User user) {
        StickerPack pack = stickerService.findPackById(dto.getPack());
        if (!pack.isBelongsTo(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
        }
        StickerPack newPack = stickerService.editPack(pack, dto.getName(), dto.getTitle());
        return ResponseEntity.ok(RestBean.success(StickerPackVO.from(newPack).build()));
    }

    @PostMapping("addStickers")
    @Secured("SCOPE_sticker.manage")
    public ResponseEntity<RestBean<List<StickerVO>>> addStickers(@RequestBody AddStickersDTO dto, @RequestAttribute("user") User user) {
        StickerPack pack = stickerService.findPackById(dto.getPack());
        if (!pack.isBelongsTo(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
        }
        List<Sticker> stickers = stickerService.createStickers(pack, dto.getStickers());
        return ResponseEntity.ok(RestBean.success(stickers.stream().map(StickerVO::from).toList()));
    }

    @GetMapping("favorite")
    @Secured("SCOPE_sticker.favorite.list")
    public ResponseEntity<RestBean<List<StickerPackVO>>> listFavoritePacks(@RequestAttribute("user") User user) {
        List<StickerPackVO> vos = stickerService.findFavorites(user).stream().map(it -> StickerPackVO.from(it).build()).toList();
        return ResponseEntity.ok(RestBean.success(vos));
    }

    @PostMapping("favorite")
    @Secured("SCOPE_sticker.favorite.manage")
    public ResponseEntity<RestBean<?>> addFavoritePack(@RequestBody AddFavoriteStickerPackDTO dto, @RequestAttribute("user") User user) {
        StickerPack pack = stickerService.findPackById(dto.getPack());
        if (pack == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Pack not found"));
        }
        stickerService.addFavorite(pack, user);
        return ResponseEntity.ok(RestBean.success("Ok"));
    }

    @DeleteMapping("favorite")
    @Secured("SCOPE_sticker.favorite.manage")
    public ResponseEntity<RestBean<?>> removeFavoritePack(@RequestBody RemoteFavoriteStickerPackDTO dto, @RequestAttribute("user") User user) {
        StickerPack pack = stickerService.findPackById(dto.getPack());
        if (pack == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Pack not found"));
        }
        stickerService.removeFavorite(pack, user);
        return ResponseEntity.ok(RestBean.success("Ok"));
    }
}
