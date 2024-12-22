package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.InviteLink;

@Data
public class InviteLinkVO {
    private String user;
    private String conversation;
    private String link;
    private long expireAt;

    public static @NotNull InviteLinkVO from(@NotNull InviteLink source) {
        InviteLinkVO vo = new InviteLinkVO();
        vo.setUser(source.getCreateBy().getUser().getId());
        vo.setConversation(source.getCreateBy().getConversation().getId());
        vo.setLink(source.getLink());
        vo.setExpireAt(source.getExpireAt().getEpochSecond());
        return vo;
    }
}
