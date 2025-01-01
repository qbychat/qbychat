package org.qbynet.chat.controller.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Bot;
import org.qbynet.chat.entity.CreateBot;
import org.qbynet.chat.entity.config.BotConfig;
import org.qbynet.chat.entity.dto.CreateBotDTO;
import org.qbynet.chat.entity.dto.DeleteBotDTO;
import org.qbynet.chat.entity.vo.BotVO;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.exception.Forbidden;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class BotController {

    @Resource
    UserService userService;

    @Resource
    BotConfig botConfig;

    @MutationMapping
    @Secured("SCOPE_bot.create")
    public BotVO createBot(@Argument CreateBotDTO input) {
        if (!botConfig.isState()) {
            throw new IllegalStateException("Bots are disabled on this instance.");
        }
        CreateBot cb;
        try {
            cb = userService.createBot(userService.currentUser(), input.getUsername(), input.getNickname());
            return BotVO.from(cb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @MutationMapping
    @Secured("SCOPE_bot.manage")
    public BotVO resetBotToken(@Argument String id) {
        if (!botConfig.isState()) {
            throw new IllegalStateException("Bots are disabled on this instance.");
        }
        Bot bot = userService.findBot(id);
        if (!bot.isBelongTo(userService.currentUser())) {
            throw new Forbidden("You are not allowed to reset this bot");
        }
        CreateBot cb;
        try {
            cb = userService.resetBotToken(bot);
            return BotVO.from(cb);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @QueryMapping
    @Secured("SCOPE_bot.list")
    public List<BotVO> bots() {
        return userService.listBots(userService.currentUser()).stream().map(BotVO::ignoreToken).toList();
    }

    @MutationMapping
    @Secured("SCOPE_bot.delete")
    public String deleteBot(@Argument @NotNull DeleteBotDTO input) {
        if (!userService.canDeleteBot(input.getBot(), userService.currentUser())) {
            throw new Forbidden("You are not allowed to delete this bot!");
        }
        userService.deleteBot(input.getBot());
        return "Deleted";
    }
}
