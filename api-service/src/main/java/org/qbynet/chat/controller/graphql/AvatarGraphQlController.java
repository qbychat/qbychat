package org.qbynet.chat.controller.graphql;

import jakarta.annotation.Resource;
import org.qbynet.chat.annotation.Authorized;
import org.qbynet.chat.entity.Avatar;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.service.AvatarService;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Authorized
@Controller
public class AvatarGraphQlController {
    @Resource
    UserService userService;

    @Resource
    ConversationService conversationService;

    @Resource
    AvatarService avatarService;


    @QueryMapping
    public List<Avatar> avatarByUser(@Argument String user) {
        return avatarService.getAllAvatars(userService.findByUsername(user));
    }

    @QueryMapping
    public List<Avatar> avatarByConversation(@Argument String conversation) {
        User self = userService.currentUser();
        return avatarService.getAllAvatars(conversationService.findConversationById(conversation), self);
    }
}
