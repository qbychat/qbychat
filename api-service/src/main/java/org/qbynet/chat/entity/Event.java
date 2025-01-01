package org.qbynet.chat.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Data
public class Event implements Serializable, Cloneable {
    private User user; // targetUser id
    private EventType type;

    private Object payload; // the addition data for the event object

    public static @NotNull Event create(@NotNull User user, EventType type, Object data) {
        Event event = new Event();
        event.user = user;
        event.type = type;
        event.payload = data;
        return event;
    }

    public Event applyUser(@NotNull User user) {
        Event event;
        try {
            event = (Event) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        event.user = user;
        return event;
    }
}
