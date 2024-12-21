package org.qbynet.chat.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class Event {
    private String user; // targetUser
    private EventType type;

    private Object data; // the addition data for the event object

    public static @NotNull Event create(@NotNull User user, EventType type, Object data) {
        Event event = new Event();
        event.user = user.getId();
        event.type = type;
        event.data = data;
        return event;
    }
}
