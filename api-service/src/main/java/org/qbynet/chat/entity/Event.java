package org.qbynet.chat.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@Data
public class Event implements Serializable, Cloneable {
    private User user; // targetUser id
    private EventType type;

    private Object payload; // the addition data for the event object

    public static @NotNull Event create(@Nullable User user, EventType type, Object payload) {
        Event event = new Event();
        event.user = user;
        event.type = type;
        event.payload = payload;
        return event;
    }

    public static @NotNull Event create(EventType eventType, Object payload) {
        return create(null, eventType, payload);
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
