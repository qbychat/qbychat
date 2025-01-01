package org.qbynet.chat.entity.event;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Message;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class DeleteMessageEvent {
    private String conversation; // conversation id
    private List<String> messages; // message ids

    public static @NotNull DeleteMessageEvent create(@NotNull Conversation conversation, @NotNull List<Message> messages) {
        DeleteMessageEvent event = new DeleteMessageEvent();
        event.setConversation(conversation.getId());
        event.setMessages(messages.stream().map(Message::getId).collect(Collectors.toList()));
        return event;
    }
}
