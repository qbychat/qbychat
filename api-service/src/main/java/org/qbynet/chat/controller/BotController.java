package org.qbynet.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.qbynet.chat.entity.CreateBot;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.config.BotConfig;
import org.qbynet.chat.entity.dto.CreateBotDTO;
import org.qbynet.chat.entity.dto.DeleteBotDTO;
import org.qbynet.chat.entity.vo.BotVO;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bot")
public class BotController {
    @Resource
    UserService userService;

    @Resource
    BotConfig botConfig;

    @PostMapping("create")
    @Secured("SCOPE_bot.create")
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

    @GetMapping("list")
    @Secured("SCOPE_bot.list")
    public ResponseEntity<RestBean<List<BotVO>>> list(@RequestAttribute("user") User user) {
        return ResponseEntity.ok(RestBean.success(userService.listBots(user).stream().map(BotVO::from).toList()));
    }

    @DeleteMapping("delete")
    @Secured("SCOPE_bot.delete")
    public ResponseEntity<RestBean<String>> delete(@RequestBody DeleteBotDTO dto, @RequestAttribute("user") User user) {
        if (!userService.canDeleteBot(dto.getBot(), user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.failure(400, "You are not allowed to delete this bot."));
        }
        userService.deleteBot(dto.getBot());
        return ResponseEntity.ok(RestBean.success("Deleted"));
    }
}
