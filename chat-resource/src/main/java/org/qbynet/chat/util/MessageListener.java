package org.qbynet.chat.util;

import lombok.extern.log4j.Log4j2;
import org.qbynet.chat.entity.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RabbitListener(queues = "qc-messages")
public class MessageListener {
    @RabbitHandler
    public void receive(Message message) {
        // todo send to pre-user
    }
}
