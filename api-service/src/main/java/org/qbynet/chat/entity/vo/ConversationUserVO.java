package org.qbynet.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.qbynet.chat.entity.ConversationType;
import org.qbynet.chat.entity.NotificationPreferment;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationUserVO {
    private String id;
    private String name;
    private ConversationType type;

    private boolean pinned;
    private NotificationPreferment notificationPreferment;
}
