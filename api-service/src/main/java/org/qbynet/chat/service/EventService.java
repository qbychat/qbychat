package org.qbynet.chat.service;

import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.EventType;
import org.qbynet.chat.entity.Status;
import org.qbynet.chat.entity.User;

public interface EventService {
    void createEvent(User user, EventType eventType, Object data);

    void createEventForRelations(@NotNull User user, EventType eventType, Status status);
}
