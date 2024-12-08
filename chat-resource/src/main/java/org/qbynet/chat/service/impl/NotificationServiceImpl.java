package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.vo.SenderVO;
import org.qbynet.chat.repository.NotificationDestinationRepository;
import org.qbynet.chat.repository.NotificationRepository;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Resource
    ConversationService conversationService;

    @Resource
    SimpMessagingTemplate messagingTemplate;

    @Resource
    NotificationRepository notificationRepository;

    @Resource
    NotificationDestinationRepository notificationDestinationRepository;

    @Override
    public void createNotification(@NotNull Message message) {
        Conversation conversation = message.getConversation();
        // create notification
        Notification notification = new Notification();
        if (message.getSender() != null) {
            notification.setSender(SenderVO.from(message.getSender()));
        } else {
            // anonymous
            notification.setSender(SenderVO.builder()
                    .conversation(conversation.getId())
                    .nickname(conversation.getName())
                    .build());
        }
        notification.setContent(message.getContent());
        notificationRepository.save(notification);

        if (conversation.isPreview()) {
            // push to all users
            messagingTemplate.convertAndSend("/conversation/messages/" + conversation.getId(), message);
        }
        notificationDestinationRepository.saveAll(conversationService.listMembers(conversation).stream().map(member -> {
            String userId = member.getUser().getId();
            if (message.getSender() == null || !userId.equals(message.getSender().getId())) {
                // the content of message is already responded in /api/message/send for sender
                messagingTemplate.convertAndSendToUser(userId, "/conversation/messages", message);
            }
            // create notification destination
            if (member.shouldPush(message)) {
                NotificationDestination dest = new NotificationDestination();
                dest.setNotification(notification.getId());
                dest.setUser(member.getUser().getId());
                return dest;
            }
            return null;
        }).filter(Objects::nonNull).toList());
    }

    @Override
    public boolean hasNotifications(@NotNull User user) {
        return notificationDestinationRepository.existsByUser(user.getId());
    }

    @Override
    public List<Notification> fetch(@NotNull User user) {
        List<NotificationDestination> notificationDestinations = notificationDestinationRepository.findAllByUser(user.getId());
        List<Notification> notifications = notificationRepository.findAllById(notificationDestinations.stream().map(NotificationDestination::getNotification).toList());
        notificationDestinationRepository.deleteAll(notificationDestinations);
        return notifications;
    }
}
