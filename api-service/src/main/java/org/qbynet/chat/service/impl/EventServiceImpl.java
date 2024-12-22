package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Event;
import org.qbynet.chat.entity.EventType;
import org.qbynet.chat.entity.Status;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.service.EventService;
import org.qbynet.chat.service.UserService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class EventServiceImpl implements EventService {
    @Lazy
    @Resource
    UserService userService;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource(name = "eventsQueue")
    Queue queue;

    @Override
    public void createEvent(User user, EventType eventType, Object data) {
        Event event = Event.create(user, eventType, data);
        log.info("Send event {} to user {}", eventType, user.getNickname());
        rabbitTemplate.convertAndSend(queue.getName(), event);
    }

    @Override
    public void createEventForRelations(@NotNull User user, EventType eventType, Status status) {
        List<User> relations = userService.collectRelations(user);
    }
}
