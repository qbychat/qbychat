package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Avatar;
import org.qbynet.chat.entity.AvatarType;

@Data
public class AvatarVO {
    private String id;

    private String user;
    private String conversation;
    private String media;
    private String compressedMedia;

    private AvatarType type;
    private long timestamp;

    public static @NotNull AvatarVO from(@NotNull Avatar source) {
        AvatarVO vo = new AvatarVO();
        vo.setId(source.getId());
        if (source.getUser() != null) {
            vo.setUser(source.getUser().getId());
        }
        if (source.getConversation() != null) {
            vo.setConversation(source.getConversation().getId());
        }
        vo.setType(source.getType());
        vo.setMedia(source.getMedia().getId());
        if (source.getMedia().getCompressed() != null) {
            vo.setCompressedMedia(source.getMedia().getCompressed().getId());
        }
        vo.setTimestamp(source.getTimestamp().getEpochSecond());
        return vo;
    }
}
