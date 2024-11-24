package org.qbynet.chat.controller;

import org.qbynet.chat.entity.dto.SendMessageDTO;
import org.qbynet.chat.entity.vo.MessageVO;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message")
public class MessageController {
    @PostMapping("send")
    public ResponseEntity<RestBean<MessageVO>> sendMessage(@RequestBody SendMessageDTO message) {
        return ResponseEntity.ok(RestBean.success());
    }
}
