package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Member;
import org.qbynet.chat.entity.MemberPermission;

import java.time.Instant;
import java.util.List;

@Data
public class MemberVO {
    private ConversationVO conversation; // conversation id

    private String nickname;
    private String title;

    private List<MemberPermission> permissions;

    private Instant joinedAt;
    private Instant muteUntil;
    private Instant banUntil;

    private boolean owner;

    public static @NotNull MemberVO from(@NotNull Member source) {
        MemberVO vo = new MemberVO();
        vo.setNickname(source.getNickname());
        vo.setTitle(source.getTitle());
        vo.setConversation(ConversationVO.from(source.getConversation()));
        vo.setPermissions(source.getPermissions());
        if (source.getMuteUntil() != null) {
            vo.setMuteUntil(source.getMuteUntil());
        }
        if (source.getBanUntil() != null) {
            vo.setBanUntil(source.getBanUntil());
        }
        vo.setOwner(source.isOwner());
        vo.setJoinedAt(source.getJoinedAt());
        return vo;
    }

    public static @NotNull MemberVO limited(@NotNull Member source) {
        MemberVO vo = new MemberVO();
        vo.setNickname(source.getNickname());
        vo.setTitle(source.getTitle());
        vo.setConversation(ConversationVO.from(source.getConversation()));
        vo.setOwner(source.isOwner());
        return vo;
    }
}
