package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.ConversationType;

@Data
public class ConversationVO {
    private String id;

    private String name;
    private String description;
    private ConversationType type;
    private String link;

    private long createdAt;
    private boolean memberVerificationNeeded;
    private boolean preview;
    private boolean noForward;

    public static @NotNull ConversationVO from(@NotNull Conversation source) {
        ConversationVO vo = new ConversationVO();
        vo.setId(source.getId());
        vo.setName(source.getName());
        vo.setDescription(source.getDescription());
        vo.setType(source.getType());
        vo.setLink(source.getLink());
        vo.setCreatedAt(source.getCreatedAt().getEpochSecond());
        vo.setMemberVerificationNeeded(source.isMemberVerificationNeeded());
        vo.setPreview(source.isPreview());
        vo.setNoForward(source.isNoForward());
        return vo;
    }
}
