package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Event;
import org.qbynet.chat.entity.EventType;

@Data
public class EventVO {
    private EventType eventType;
    private Object payload;

    public static @NotNull EventVO from(@NotNull Event event) {
        EventVO vo = new EventVO();
        vo.setEventType(event.getType());
        vo.setPayload(event.getPayload());
        return vo;
    }
}
