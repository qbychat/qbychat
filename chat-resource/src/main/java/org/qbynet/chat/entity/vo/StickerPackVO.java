package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.StickerPack;

@Data
public class StickerPackVO {
    private String id;

    private String name;
    private String link;
    private String thumbnail;

    private String telegramUpstream;

    public static @NotNull StickerPackVO from(@NotNull StickerPack source) {
        StickerPackVO vo = new StickerPackVO();
        vo.setId(source.getId());
        vo.setName(source.getTitle());
        vo.setLink(source.getName());
        if (source.getThumbnail() != null) {
            vo.setThumbnail(source.getThumbnail().getId());
        }
        vo.setTelegramUpstream(source.getTelegramUpstream());
        return vo;
    }
}
