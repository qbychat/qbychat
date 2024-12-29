package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.JoinRequest;

import java.time.Instant;

@Data
public class JoinRequestVO {
    private String id;

    private UserVO user;
    private ConversationVO conversation;

    private Instant timestamp;

    public static @NotNull JoinRequestVO from(@NotNull JoinRequest source) {
        JoinRequestVO vo = new JoinRequestVO();
        vo.setId(source.getId());
        vo.setTimestamp(source.getTimestamp());
        vo.setUser(UserVO.from(source.getUser()));
        vo.setConversation(ConversationVO.from(source.getConversation()));
        return vo;
    }
}
