package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.JoinRequest;

@Data
public class JoinRequestVO {
    private String id;

    private String user; // user id
    private String conversation; // conversation id

    public static @NotNull JoinRequestVO from(@NotNull JoinRequest source) {
        JoinRequestVO vo = new JoinRequestVO();
        vo.setId(source.getId());
        vo.setUser(source.getUser().getId());
        vo.setConversation(source.getConversation().getId());
        return vo;
    }
}
