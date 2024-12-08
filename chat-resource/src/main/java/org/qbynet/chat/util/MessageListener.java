package org.qbynet.chat.util;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RabbitListener(queues = "qc-messages")
public class MessageListener {

    @Resource
    NotificationService notificationService;

    @RabbitHandler
    public void receive(@NotNull Message message) {
        // push notifications
        notificationService.createNotification(message);
    }
}
