package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Sticker;

@Data
public class StickerVO {
    private String id;
    private String emoji;
    private String media; // media id
    private String pack; // pack id

    public static @NotNull StickerVO from(@NotNull Sticker source) {
        StickerVO vo = new StickerVO();
        vo.setId(source.getId());
        vo.setEmoji(source.getEmoji());
        vo.setMedia(source.getMedia().getId());
        vo.setPack(source.getPack().getId());
        return vo;
    }
}
