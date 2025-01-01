package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Contact;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.ConversationType;
import org.qbynet.chat.entity.Member;

import java.time.Instant;

@Data
public class ConversationVO {
    private String id;

    private String name;
    private String description;
    private ConversationType type;
    private String link;

    private Instant createdAt;
    private boolean verifyNeeded;
    private boolean preview;
    private boolean noForward;

    private MemberVO pmPartner;

    public static @NotNull ConversationVO from(@NotNull Conversation source) {
        ConversationVO vo = new ConversationVO();
        vo.setId(source.getId());
        vo.setName(source.getName());
        vo.setDescription(source.getDescription());
        vo.setType(source.getType());
        vo.setLink(source.getLink());
        vo.setCreatedAt(source.getCreatedAt());
        vo.setVerifyNeeded(source.isVerifyNeeded());
        vo.setPreview(source.isPreview());
        vo.setNoForward(source.isNoForward());
        return vo;
    }

    public static @NotNull ConversationVO privateChat(@NotNull Member partner, Contact contact) {
        ConversationVO vo = from(partner.getConversation());
        // nickname
        if (contact != null && contact.getRemark() != null) {
            vo.setName(contact.getRemark());
        } else {
            vo.setName(partner.getNickname());
        }
        // bio
        vo.setDescription(partner.getUser().getBio());
        vo.setPmPartner(MemberVO.from(partner));
        return vo;
    }
}
