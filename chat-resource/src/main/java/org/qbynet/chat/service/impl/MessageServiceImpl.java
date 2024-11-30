package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.repository.MessageRepository;
import org.qbynet.chat.service.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Log4j2
@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    MessageRepository messageRepository;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Override
    public Message send(Message source) {
        Message message = messageRepository.save(source);
        rabbitTemplate.convertAndSend(message);
        return message;
    }

    @RabbitListener
    private void onMessage(Message message) {
        log.info("Message received: {}", message.getId());
    }

    @Scheduled(cron = "0 0 */6 * * *")
    private void autoDeleteExpiredMessages() {
        log.info("Delete expired messages");
        messageRepository.deleteByExpiresAtLessThan(Instant.now());
    }
}
