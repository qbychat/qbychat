package org.qbynet.chat.util;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.Event;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "qc-events")
public class EventListener {
    @Resource
    SimpMessagingTemplate messagingTemplate;

    @RabbitHandler
    public void process(Event message) {
        messagingTemplate.convertAndSendToUser(message.getUser(), "/topic/events", message);
    }
}
