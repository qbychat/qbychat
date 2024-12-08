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

@RestController
@RequestMapping("/api/message")
public class MessageController {
    @Resource
    MessageService messageService;

    @PostMapping("send")
    public ResponseEntity<RestBean<MessageVO>> sendMessage(@RequestBody SendMessageDTO message, @RequestAttribute("user") User user) {
        if (message.getConversation() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "The given conversation must not be null"));
        }
        Conversation conversation = messageService.findConversationById(message.getConversation());
        if (conversation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Conversation not found"));
        }
        if (!messageService.canSendMessage(conversation, user)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(RestBean.failure(503, "Cannot send message"));
        }
        try {
            Message msg = messageService.send(message, user);
            return ResponseEntity.ok(RestBean.success(MessageVO.from(msg)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage()));
        }
    }

    @PostMapping("read")
    public ResponseEntity<RestBean<String>> markAsRead(@RequestBody ReadMessageDTO dto, @RequestAttribute("user") User user) {
        messageService.markAsRead(dto.getMessages(), user);
        return ResponseEntity.ok(RestBean.success("Ok"));
    }
}
