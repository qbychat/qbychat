package org.qbynet.chat.entity.event;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Message;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ReadMessageEvent {
    private List<String> messages;

    public static @NotNull ReadMessageEvent create(@NotNull List<Message> messages) {
        ReadMessageEvent event = new ReadMessageEvent();
        event.setMessages(messages.stream().map(Message::getId).collect(Collectors.toList()));
        return event;
    }
}
