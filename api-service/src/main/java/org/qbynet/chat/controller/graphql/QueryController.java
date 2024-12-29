package org.qbynet.chat.controller.graphql;

import org.qbynet.chat.entity.Avatar;
import jakarta.annotation.Resource;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.FetchAvatarDTO;
import org.qbynet.chat.service.AvatarService;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

@Controller
public class QueryController {
    @Resource
    UserService userService;

    @Resource
    ConversationService conversationService;

    @Resource
    AvatarService avatarService;

    @QueryMapping
    @Secured("ROLE_USER")
    public User myself() {
        return userService.currentUser();
    }

    @QueryMapping
    @Secured("ROLE_USER")
    public List<Conversation> conversationList() {
        return conversationService.list(userService.currentUser());
    }

    @QueryMapping
    @Secured("ROLE_USER")
    public DeferredResult<List<Avatar>> avatarByUser(@Argument FetchAvatarDTO input) {
        DeferredResult<List<Avatar>> result = new DeferredResult<>();
        if(input.getUserId()!=null){
            User user = userService.findByUsername(input.getUserId());
            result.setResult(avatarService.getAllAvatars(user));
        }else if(input.getConversationId()!=null){
            Conversation conversation = conversationService.findConversationById(input.getConversationId());
            result.setResult(avatarService.getAllAvatars(conversation));
        }else{
            result.setErrorResult(new IllegalArgumentException("Bad request"));
        }
        return result;
    }
}
