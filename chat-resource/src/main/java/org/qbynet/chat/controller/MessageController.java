package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.EditMessageDTO;
import org.qbynet.chat.entity.dto.FetchMessageDTO;
import org.qbynet.chat.entity.dto.ReadMessageDTO;
import org.qbynet.chat.entity.dto.SendMessageDTO;
import org.qbynet.chat.entity.vo.MessageVO;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.MessageService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
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

    @PostMapping("send")
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
        return ResponseEntity.ok(RestBean.success("Ok"));
    }

    @GetMapping("fetch")
    public ResponseEntity<RestBean<List<MessageVO>>> fetchMessages(@RequestParam FetchMessageDTO dto, @RequestAttribute("user") User user) {
        Conversation conversation1 = conversationService.findConversationById(dto.getConversationId());
        if (conversation1 == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Conversation not found"));
        }
        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize());
        Page<Message> messages = messageService.fetchMessages(conversation1, user, pageable);
        if (messages == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "Not found"));
        }
        List<MessageVO> result = new ArrayList<>();
        messages.getContent().forEach(i -> result.add(MessageVO.from(i)));
        return ResponseEntity.ok(RestBean.success(result));
    }
}
