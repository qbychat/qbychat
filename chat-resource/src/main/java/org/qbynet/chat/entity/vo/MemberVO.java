package org.qbynet.chat.entity.vo;

import lombok.Builder;
import lombok.Data;
import org.qbynet.chat.entity.MemberPermission;

import java.util.List;

@Data
@Builder
public class MemberVO {
    private ConversationVO conversation;

    private String nickname;
    private String title;

    private List<MemberPermission> permissions;

    private long joinedAt;
    private long muteUntil;
    private long banUntil;

    private boolean owner;
}
