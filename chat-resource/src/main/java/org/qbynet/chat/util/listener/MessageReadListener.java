package org.qbynet.chat.util.listener;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.ReadMessage;
import org.qbynet.chat.service.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "qc-message-reads")
public class MessageReadListener {

    @Resource
    MessageService messageService;

    @RabbitHandler
    public void receive(@NotNull ReadMessage dto) {
        messageService.markAsRead(dto.getMessages(), dto.getUser());
    }

}
