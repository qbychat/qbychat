package org.qbynet.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.qbynet.chat.entity.CreateBot;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.CreateBotDTO;
import org.qbynet.chat.entity.vo.BotVO;
import org.qbynet.chat.service.UserService;
import org.qbynet.chat.util.BotConfig;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@Secured("SCOPE_bot")
@RestController
@RequestMapping("/api/bot")
public class BotController {
    @Resource
    UserService userService;

    @Resource
    BotConfig botConfig;

    @PostMapping("create")
    public ResponseEntity<RestBean<BotVO>> create(@RequestBody CreateBotDTO dto, @RequestAttribute("user") User user) {
        if (!botConfig.isState()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Bots are disabled on this instance."));
        }
        CreateBot cb;
        try {
            cb = userService.createBot(user, dto.getUsername(), dto.getNickname());
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestBean.failure(500, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage()));
        }
        return ResponseEntity.ok(RestBean.success(BotVO.from(cb)));
    }
}
