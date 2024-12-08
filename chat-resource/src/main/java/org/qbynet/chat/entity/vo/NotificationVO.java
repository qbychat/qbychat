package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Notification;
import org.qbynet.chat.entity.NotificationType;

@Data
public class NotificationVO {
    private String content;
    private SenderVO sender;

    private NotificationType type;

    public static @NotNull NotificationVO from(@NotNull Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setContent(notification.getContent());
        vo.setSender(notification.getSender());
        vo.setType(notification.getType());
        return vo;
    }
}
