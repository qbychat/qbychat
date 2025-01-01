package org.qbynet.chat.entity.event;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Conversation;

@Data
public class ClearHistoryEvent {
    private String conversation; // conversation id

    public static @NotNull ClearHistoryEvent create(@NotNull Conversation conversation) {
        ClearHistoryEvent event = new ClearHistoryEvent();
        event.setConversation(conversation.getId());
        return event;
    }
}
