package org.qbynet.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Member;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderVO {
    private String user;

    private String nickname;
    private String title;

    public static @NotNull SenderVO from(@NotNull Member member) {
        SenderVO vo = new SenderVO();
//        vo.setConversation(member.getConversation().getId());
        vo.setUser(member.getUser().getId());
        vo.setNickname(member.getNickname());
        vo.setTitle(member.getTitle());
        return vo;
    }
}
