package org.qbynet.chat.util;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.JoinRequest;
import org.qbynet.chat.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "qc-join-request")
public class JoinRequestListener {
    @Resource
    NotificationService notificationService;

    @RabbitHandler
    public void process(JoinRequest joinRequest) {
        notificationService.createNotification(joinRequest);
    }
}
