package org.qbynet.chat.entity.vo;

import lombok.Builder;
import lombok.Data;
import org.qbynet.chat.entity.ConversationType;

@Data
@Builder
public class ConversationVO {
    private String id;

    private String name;
    private String description;
    private ConversationType type;
    private String link;

    private long createdAt;
    private boolean preview;
    private boolean noForward;
    private boolean hideMembers;
}
