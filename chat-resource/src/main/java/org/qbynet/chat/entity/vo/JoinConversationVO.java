package org.qbynet.chat.entity.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinConversationVO {
    private boolean joined;
    private boolean banned;
}
