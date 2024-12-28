package org.qbynet.chat.controller.graphql;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.EditMessageDTO;
import org.qbynet.chat.entity.dto.FetchMessageDTO;
import org.qbynet.chat.entity.dto.SendMessageDTO;
import org.qbynet.chat.entity.vo.MessageVO;
import org.qbynet.chat.exception.BadRequest;
import org.qbynet.chat.exception.Forbidden;
import org.qbynet.chat.exception.NotFound;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.MessageService;
import org.qbynet.chat.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.async.DeferredResult;

@Controller
public class MessageGraphQLController {
    @Resource
    MessageService messageService;
    @Resource
    UserService userService;
    @Resource
    ConversationService conversationService;

    @MutationMapping
    @Secured("SCOPE_message.send") // client->server
    public MessageVO sendMessage(@Argument @NotNull SendMessageDTO input) {
        User user = userService.currentUser();
        if (input.getConversation() == null) {
            throw new BadRequest("Conversation cannot be null");
        }
        Conversation conversation = conversationService.findConversationById(input.getConversation());
        if (conversation == null) {
            throw new NotFound("Conversation not found!");
        }
        if (!messageService.canSendMessage(conversation, user)) {
            throw new Forbidden("You couldn't send message now!");
        }
        try {
            Message msg = messageService.send(input);
            return MessageVO.from(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @MutationMapping
    @Secured("SCOPE_message.edit")
    public MessageVO editMessage(@Argument @NotNull EditMessageDTO input) {
        Message message = messageService.editMessage(input);
        return MessageVO.from(message);
    }

    @QueryMapping
    @Secured("SCOPE_message.fetch")
    public DeferredResult<MessageVO> fetchMessage(@Argument FetchMessageDTO input) {
        // todo
        throw new RuntimeException("TODO!");
    }
}
