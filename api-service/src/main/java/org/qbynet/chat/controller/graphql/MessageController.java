package org.qbynet.chat.controller.graphql;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.DeleteMessageDTO;
import org.qbynet.chat.entity.dto.EditMessageDTO;
import org.qbynet.chat.entity.dto.FetchMessageDTO;
import org.qbynet.chat.entity.dto.SendMessageDTO;
import org.qbynet.chat.entity.vo.MessageVO;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.MessageService;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.exception.BadRequest;
import org.qbynet.shared.exception.Forbidden;
import org.qbynet.shared.exception.NotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class MessageController {
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
            return messageService.toMessageVO(msg, user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @MutationMapping
    @Secured("SCOPE_message.edit")
    public MessageVO editMessage(@Argument @NotNull EditMessageDTO input) {
        User user = userService.currentUser();
        Message message = messageService.editMessage(input);
        return messageService.toMessageVO(message, user);
    }

    @MutationMapping
    @Secured("SCOPE_message.delete")
    public String deleteMessage(@Argument @NotNull DeleteMessageDTO input) {
        messageService.delete(input);
        return "Success";
    }

    @MutationMapping
    @Secured("SCOPE_message.read")
    public String markAsRead(@Argument @NotNull List<String> messages) {
        messageService.markAsRead(messages);
        return "Success";
    }

    @QueryMapping
    @Secured("SCOPE_message.fetch")
    public List<MessageVO> fetchMessage(@Argument @NotNull FetchMessageDTO input) {
        User user = userService.currentUser();
        Conversation conversation = conversationService.findConversationById(input.getConversation());
        Pageable pageable = PageRequest.of(input.getPage(), input.getSize());
        Page<Message> messages;
        if (input.getSince() != null) {
            Message sinceMessage = messageService.findMessageById(input.getSince());
            messages = messageService.fetchMessages(conversation, sinceMessage, user, pageable);
        } else {
            messages = messageService.fetchMessages(conversation, user, pageable);
        }
        if (conversation == null) {
            throw new NotFound("Conversation not found!");
        }
        // check permission
        if (conversationService.hasViewPermission(conversation, user)) {
            throw new Forbidden("No permission to view messages!");
        }
        if (messages == null) {
            throw new BadRequest("Cannot fetch messages");
        }
        return messages.stream().map(message -> messageService.toMessageVO(message, user)).toList();
    }
}
