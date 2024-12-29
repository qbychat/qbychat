package org.qbynet.chat.entity;

import lombok.Builder;
import lombok.Data;
import org.qbynet.chat.entity.vo.ConversationVO;
import org.qbynet.chat.entity.vo.MemberVO;

@Data
@Builder
public class ConversationSession {
    private ConversationVO conversation;
    private MemberVO member;

    private int joinRequests; // count of join requests (need permission)
    private boolean joined; // is joined conversation
    private boolean joinRequestPending; // current join request
}
