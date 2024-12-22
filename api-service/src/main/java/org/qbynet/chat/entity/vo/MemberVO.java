package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Member;
import org.qbynet.chat.entity.MemberPermission;

import java.util.List;

@Data
public class MemberVO {
    private String conversation; // conversation id

    private String nickname;
    private String title;

    private List<MemberPermission> permissions;

    private long joinedAt;
    private long muteUntil;
    private long banUntil;

    private boolean owner;

    public static @NotNull MemberVO from(@NotNull Member source) {
        MemberVO vo = new MemberVO();
        vo.setNickname(source.getNickname());
        vo.setTitle(source.getTitle());
        vo.setConversation(source.getConversation().getId());
        vo.setPermissions(source.getPermissions());
        if (source.getMuteUntil() != null) {
            vo.setMuteUntil(source.getMuteUntil().toEpochMilli());
        }
        if (source.getBanUntil() != null) {
            vo.setBanUntil(source.getBanUntil().toEpochMilli());
        }
        vo.setOwner(source.isOwner());
        vo.setJoinedAt(source.getJoinedAt().toEpochMilli());
        return vo;
    }
}
