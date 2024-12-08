package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.ReadMessageDTO;
import org.qbynet.chat.entity.dto.SendMessageDTO;
import org.qbynet.chat.entity.vo.MessageVO;
import org.qbynet.chat.service.MessageService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ForkJoinPool;

@RestController
@RequestMapping("/api/message")
public class MessageController {
    @Resource
    MessageService messageService;

    @PostMapping("send")
    public DeferredResult<ResponseEntity<RestBean<MessageVO>>> sendMessage(@RequestBody SendMessageDTO message, @RequestAttribute("user") User user) {
        DeferredResult<ResponseEntity<RestBean<MessageVO>>> result = new DeferredResult<>();
        if (message.getConversation() == null) {
            result.setErrorResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "The given conversation must not be null")));
            return result;
        }
        Conversation conversation = messageService.findConversationById(message.getConversation());
        if (conversation == null) {
            result.setErrorResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Conversation not found")));
            return result;
        }
        if (!messageService.canSendMessage(conversation, user)) {
            result.setErrorResult(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(RestBean.failure(503, "Cannot send message")));
            return result;
        }

        result.onTimeout(() -> result.setResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(RestBean.failure(408, "Timeout"))));

        ForkJoinPool.commonPool().submit(() -> {
            try {
                Message msg = messageService.send(message, user);
                result.setResult(ResponseEntity.ok(RestBean.success(MessageVO.from(msg))));
            } catch (Exception e) {
                result.setErrorResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage())));
            }
        });
        return result;
    }

    @PostMapping("read")
    public ResponseEntity<RestBean<String>> markAsRead(@RequestBody ReadMessageDTO dto, @RequestAttribute("user") User user) {
        messageService.markAsRead(dto.getMessages(), user);
        return ResponseEntity.ok(RestBean.success("Ok"));
    }
}
