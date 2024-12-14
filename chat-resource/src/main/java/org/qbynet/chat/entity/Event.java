package org.qbynet.chat.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class Event {
    private String user; // targetUser
    private EventType type;

    public static @NotNull Event create(@NotNull User user, EventType type) {
        Event event = new Event();
        event.user = user.getId();
        event.type = type;
        return event;
    }
}
