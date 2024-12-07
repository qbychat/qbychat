package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Member;

@Data
public class SenderVO {
    private String conversation;
    private String user;

    private String nickname;
    private String title;

    public static @NotNull SenderVO from(@NotNull Member member) {
        SenderVO vo = new SenderVO();
        vo.setConversation(member.getConversation().getId());
        vo.setUser(member.getUser().getId());
        vo.setNickname(member.getNickname());
        vo.setTitle(member.getTitle());
        return vo;
    }
}
