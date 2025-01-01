package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.EventService;
import org.qbynet.chat.service.UserService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Log4j2
@Service
public class EventServiceImpl implements EventService {
    @Lazy
    @Resource
    UserService userService;

    @Lazy
    @Resource
    ConversationService conversationService;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource
    SimpUserRegistry simpUserRegistry;

    @Resource(name = "eventsQueue")
    Queue queue;

    @Override
    public void createEvent(User dest, EventType eventType, Object data) {
        Event event = Event.create(dest, eventType, data);
        pushEvent(event);
    }

    @Override
    public void createEvent(User dest, @NotNull Event exist) {
        pushEvent(exist.applyUser(dest));
    }

    private void pushEvent(@NotNull Event event) {
        log.info("Send event {} to user {}", event.getType(), event.getUser().getId());
        rabbitTemplate.convertAndSend(queue.getName(), event);
    }

    @Override
    public void updateConversationStatus(@NotNull User user) {
        // check is really disconnected
        if (simpUserRegistry.getUser(user.getRemoteId()) == null) {
            // disconnected
            userService.goneOffline(user);
        }
        conversationService.listJoinedConversations(user).forEach(member -> {
            Conversation conversation = member.getConversation();
            switch (conversation.getType()) {
                case GROUP -> {
                    List<Member> members = conversationService.listMembers(conversation);
                    Event event = new Event();
                    event.setType(EventType.UPDATE_CONVERSATION_STATUS);
                    List<MemberActivity> activities = List.of(); // todo activities
                    event.setPayload(ConversationStatus.create(conversation, members.size(), (int) members.stream().map(it ->
                                simpUserRegistry.getUser(it.getUser().getRemoteId()))
                            .filter(Objects::nonNull)
                            .count(),
                        activities
                    ));
                    members.forEach(conversationMember -> {
                        // send the new status for each member
                        createEvent(conversationMember.getUser(), event);
                    });
                }
                case PRIVATE_CHAT -> {
                    Member partner = conversationService.getPrivateChatPartner(conversation, user);
                    Status status = null;
                    List<MemberActivity> activities = List.of(); // todo activities
                    if (userService.canAccessStatus(partner.getUser(), user)) {
                        status = partner.getUser().getStatus();
                    }
                    boolean isOnline = simpUserRegistry.getUser(partner.getUser().getRemoteId()) != null;
                    Instant lastOnline = null;
                    if (!isOnline) {
                        if (userService.canAccessOnlineStatus(partner.getUser(), user)) {
                            lastOnline = partner.getUser().getLastLoginTime();
                        }
                    }
                    Event event = Event.create(partner.getUser(), EventType.UPDATE_CONVERSATION_STATUS, ConversationStatus.create(
                        conversation, -1, -1, isOnline,
                        lastOnline, status, activities
                    ));
                    pushEvent(event);
                }
            }
        });
    }
}
