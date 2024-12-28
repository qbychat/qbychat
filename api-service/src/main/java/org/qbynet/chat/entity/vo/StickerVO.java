package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Sticker;

@Data
public class StickerVO {
    private String id;
    private String emoji;
    private MediaVO media; // media id
    private StickerPackVO pack; // pack id

    public static @NotNull StickerVO from(@NotNull Sticker source) {
        StickerVO vo = new StickerVO();
        vo.setId(source.getId());
        vo.setEmoji(source.getEmoji());
        vo.setMedia(MediaVO.from(source.getMedia()));
        if (!source.getPack().getThumbnail().equals(source)) {
            vo.setPack(StickerPackVO.from(source.getPack()).build());
        }
        return vo;
    }
}
