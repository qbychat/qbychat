package org.qbynet.chat.entity.event;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Conversation;

@Data
public class DisbandConversationEvent {
    private String conversation; // conversation id

    public static @NotNull DisbandConversationEvent create(@NotNull Conversation conversation) {
        DisbandConversationEvent event = new DisbandConversationEvent();
        event.setConversation(conversation.getId());
        return event;
    }
}
