package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.event.ClearHistoryEvent;
import org.qbynet.chat.entity.event.DeleteMessageEvent;
import org.qbynet.chat.entity.event.ReadMessageEvent;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.EventService;
import org.qbynet.chat.service.UserService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

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

    private @NotNull Map<Conversation, List<Message>> classifyMessagesByConversation(@NotNull List<Message> messages) {
        Map<Conversation, List<Message>> conversationMap = new HashMap<>();

        for (Message message : messages) {
            Conversation conversation = message.getConversation();
            conversationMap.computeIfAbsent(conversation, k -> new ArrayList<>());
            conversationMap.get(conversation).add(message);
        }

        return conversationMap;
    }

    private @NotNull Map<User, List<Message>> classifyMessagesBySender(@NotNull List<Message> messages) {
        Map<User, List<Message>> userMap = new HashMap<>();

        for (Message message : messages) {
            User user = message.getSender().getUser();
            userMap.computeIfAbsent(user, k -> new ArrayList<>());
            userMap.get(user).add(message);
        }

        return userMap;
    }

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
                    List<MemberActivity> activities = conversationService.listActivities(conversation);
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
                    List<MemberActivity> activities = conversationService.listActivities(conversation);
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

    @Override
    public void clearHistory(@NotNull Conversation conversation) {
        Event event = Event.create(EventType.CLEAR_HISTORY, ClearHistoryEvent.create(conversation));
        pushEventToMembers(conversation, event);
    }

    @Override
    public void deleteMessages(List<Message> messages) {
        classifyMessagesByConversation(messages).forEach((conversation, sortedMessages) -> {
            Event event = Event.create(EventType.MESSAGE_DELETED, DeleteMessageEvent.create(conversation, sortedMessages));
            pushEventToMembers(conversation, event);
        });
    }

    @Override
    public void markAsRead(List<Message> messages) {
        classifyMessagesBySender(messages).forEach((user, sortedMessages) ->
            Event.create(user, EventType.MESSAGE_READ, ReadMessageEvent.create(sortedMessages))
        );
    }

    /**
     * Push event message to every member
     *
     * @param conversation the conversation
     * @param event        the event
     */
    private void pushEventToMembers(@NotNull Conversation conversation, Event event) {
        conversationService.listMembers(conversation).forEach(member -> pushEvent(event.applyUser(member.getUser())));
    }
}
