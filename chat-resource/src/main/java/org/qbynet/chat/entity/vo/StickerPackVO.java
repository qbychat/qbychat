package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.StickerPack;

@Data
public class StickerPackVO {
    private String id;

    private String name;
    private String link;

    private String telegramUpstream;

    public static @NotNull StickerPackVO from(@NotNull StickerPack source) {
        StickerPackVO vo = new StickerPackVO();
        vo.setId(source.getId());
        vo.setName(source.getName());
        vo.setLink(source.getLink());
        vo.setTelegramUpstream(source.getTelegramUpstream());
        return vo;
    }
}
