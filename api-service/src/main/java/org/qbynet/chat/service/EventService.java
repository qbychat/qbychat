package org.qbynet.chat.service;

import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;

import java.util.List;

public interface EventService {
    void createEvent(User user, EventType eventType, Object data);

    void createEvent(User dest, @NotNull Event exist);

    void updateConversationStatus(User user);

    void clearHistory(@NotNull Conversation conversation);

    void deleteMessages(List<Message> messages);
}
