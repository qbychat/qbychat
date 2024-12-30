package org.qbynet.chat.controller.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.qbynet.chat.annotation.Authorized;
import org.qbynet.chat.entity.Bot;
import org.qbynet.chat.entity.CreateBot;
import org.qbynet.chat.entity.config.BotConfig;
import org.qbynet.chat.entity.dto.CreateBotDTO;
import org.qbynet.chat.entity.dto.DeleteBotDTO;
import org.qbynet.chat.entity.vo.BotVO;
import org.qbynet.chat.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

@Controller
public class BotGraphQLController {

    @Resource
    UserService userService;

    @Resource
    BotConfig botConfig;

    @MutationMapping
    @Secured("SCOPE_bot.create")
    public DeferredResult<BotVO> createBot(@Argument CreateBotDTO input) {
        DeferredResult<BotVO> result = new DeferredResult<>();
        if(!botConfig.isState()) {
            result.setErrorResult(new IllegalStateException("Bots are disabled on this instance."));
        }
        CreateBot cb;
        try {
            cb = userService.createBot(userService.currentUser(), input.getUsername(), input.getNickname());
            result.setResult(BotVO.from(cb.getBot()));
        } catch (Exception e) {
            result.setErrorResult(e);
        }
        return result;
    }

    @QueryMapping
    @Secured("SCOPE_bot.list")
    public List<BotVO> botList() {
        return userService.listBots(userService.currentUser()).stream().map(BotVO::from).toList();
    }

    @MutationMapping
    @Secured("SCOPE_bot.delete")
    public DeferredResult<String> deleteBot(@Argument DeleteBotDTO input) {
        DeferredResult<String> result = new DeferredResult<>();
        if (!userService.canDeleteBot(input.getBot(), userService.currentUser())) {
            result.setErrorResult("You are not allowed to delete this bot!");
        }
        userService.deleteBot(input.getBot());
        result.setResult("Deleted");
        return result;
    }
}
