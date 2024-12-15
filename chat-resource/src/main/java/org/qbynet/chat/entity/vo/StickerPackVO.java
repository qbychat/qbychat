package org.qbynet.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.StickerPack;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StickerPackVO {
    private String id;

    private String name;
    private String link;
    private String thumbnail;

    @Builder.Default
    private List<String> stickers = List.of();
    @Builder.Default
    private int uses = -1;

    private String telegramUpstream;

    public static @NotNull StickerPackVOBuilder from(@NotNull StickerPack source) {
        StickerPackVO.StickerPackVOBuilder builder = StickerPackVO.builder()
                .id(source.getId()).name(source.getTitle())
                .link(source.getName());
        if (source.getThumbnail() != null) {
            builder.thumbnail(source.getThumbnail().getId());
        }
        return builder.telegramUpstream(source.getTelegramUpstream());
    }
}
