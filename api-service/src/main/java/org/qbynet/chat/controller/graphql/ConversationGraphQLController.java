package org.qbynet.chat.controller.graphql;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.InviteLink;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.CreateConversationDTO;
import org.qbynet.chat.entity.dto.InviteDTO;
import org.qbynet.chat.entity.vo.ConversationVO;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class ConversationGraphQLController {
    @Resource
    UserService userService;

    @Resource
    ConversationService conversationService;

    @MutationMapping
    @Secured("SCOPE_conversation.create")
    public ConversationVO createConversation(@Argument @NotNull CreateConversationDTO input) {
        User user = userService.currentUser();
        Conversation conversation = conversationService.create(input.getName(), input.getType(), user);
        return ConversationVO.from(conversation);
    }

    @QueryMapping
    @PreAuthorize("isFullyAuthenticated()")
    public ConversationVO conversationByLink(@Argument @NotNull String link) {
        return ConversationVO.from(conversationService.findByLink(link));
    }

    @MutationMapping
    @Secured("SCOPE_conversation.invite")
    public InviteLink invite(@Argument @NotNull InviteDTO input) {
        return conversationService.invite(input);
    }
}
