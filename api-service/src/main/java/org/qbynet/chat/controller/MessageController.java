package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.entity.ReadMessage;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.EditMessageDTO;
import org.qbynet.chat.entity.dto.ReadMessageDTO;
import org.qbynet.chat.entity.dto.SendMessageDTO;
import org.qbynet.chat.entity.vo.MessageVO;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.MessageService;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/message")
public class MessageController {
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Resource
    MessageService messageService;

    @Resource
    ConversationService conversationService;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource
    UserService userService;

    @Resource(name = "messageReadQueue")
    Queue messageReadQueue;

    @PostMapping("send")
    @Secured("SCOPE_message.send")
    public DeferredResult<ResponseEntity<RestBean<MessageVO>>> sendMessage(@RequestBody SendMessageDTO message, @RequestAttribute("user") User user) {
        DeferredResult<ResponseEntity<RestBean<MessageVO>>> result = new DeferredResult<>();
        if (message.getConversation() == null) {
            result.setErrorResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "The given conversation must not be null")));
            return result;
        }
        Conversation conversation = conversationService.findConversationById(message.getConversation());
        if (conversation == null) {
            result.setErrorResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Conversation not found")));
            return result;
        }
        if (!messageService.canSendMessage(conversation, user)) {
            result.setErrorResult(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(RestBean.failure(503, "Cannot send message")));
            return result;
        }

        result.onTimeout(() -> result.setResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(RestBean.failure(408, "Timeout"))));

        executorService.submit(() -> {
            try {
                Message msg = messageService.send(message, user);
                result.setResult(ResponseEntity.ok(RestBean.success(MessageVO.from(msg))));
            } catch (Exception e) {
                result.setErrorResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage())));
            }
        });
        return result;
    }

    @PostMapping("edit")
    @Secured("SCOPE_message.edit")
    public DeferredResult<ResponseEntity<RestBean<MessageVO>>> editMessage(@RequestBody EditMessageDTO dto, @RequestAttribute("user") User user) {
        DeferredResult<ResponseEntity<RestBean<MessageVO>>> result = new DeferredResult<>();
        executorService.submit(() -> {
            Message message = messageService.findMessageById(dto.getMessage());
            if (!message.isBelongsTo(user)) {
                result.setErrorResult(ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.failure(403, "Forbidden")));
                return;
            }
            messageService.editMessage(message, dto.getContent(), dto.getSticker(), dto.getMedias(), dto.isLinkPreview());
        });
        return result;
    }

    @PostMapping("read")
    public ResponseEntity<RestBean<?>> markAsRead(@RequestBody ReadMessageDTO dto, @RequestAttribute("user") User user) {
        messageService.markAsRead(dto.getMessages(), user);
        rabbitTemplate.convertAndSend(messageReadQueue.getName(), ReadMessage.builder().messages(dto.getMessages()).user(user).build());
        return ResponseEntity.ok(RestBean.success("Ok"));
    }

    @GetMapping("fetch")
    public ResponseEntity<RestBean<List<MessageVO>>> fetchMessages(@RequestParam(name = "conversation") String conversationId, @RequestParam(required = false) String since, @RequestParam int page, @RequestParam(required = false, defaultValue = "100") int size, @RequestAttribute("user") User user) {
        Conversation conversation = conversationService.findConversationById(conversationId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages;
        if (since != null) {
            Message sinceMessage = messageService.findMessageById(since);
            messages = messageService.fetchMessages(conversation, sinceMessage, user, pageable);
        } else {
            messages = messageService.fetchMessages(conversation, user, pageable);
        }
        if (conversation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Conversation not found"));
        }
        if (messages == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.failure(403, "No permission to view messages"));
        }
        return ResponseEntity.ok(RestBean.success(messages.stream().map(message -> MessageVO.builder(message)
            .bot(userService.isBot(message.getSender().getUser()))
            .myself(message.getSender().getUser().equals(user))
            .build()
        ).toList()));
    }
}
