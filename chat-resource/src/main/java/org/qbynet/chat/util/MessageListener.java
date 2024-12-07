package org.qbynet.chat.util;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.service.ConversationService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RabbitListener(queues = "qc-messages")
public class MessageListener {
    @Resource
    SimpMessagingTemplate messagingTemplate;

    @Resource
    ConversationService conversationService;

    @RabbitHandler
    public void receive(@NotNull Message message) {
        Conversation conversation = message.getConversation();
        conversationService.listMembers(conversation).forEach(member -> {
            String userId = member.getUser().getId();
            if (message.getSender() == null || !userId.equals(message.getSender().getId())) {
                // the content of message is already responded in /api/message/send for sender
                messagingTemplate.convertAndSendToUser(userId, "/conversation/messages", message);
            }
        });
    }
}
