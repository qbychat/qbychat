package org.qbynet.chat.service;

import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Event;
import org.qbynet.chat.entity.EventType;
import org.qbynet.chat.entity.User;

public interface EventService {
    void createEvent(User user, EventType eventType, Object data);

    void createEvent(User dest, @NotNull Event exist);

    void updateConversationStatus(User user);
}
