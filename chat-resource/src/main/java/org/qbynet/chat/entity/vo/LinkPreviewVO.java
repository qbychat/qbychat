package org.qbynet.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.qbynet.chat.entity.LinkPreview;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkPreviewVO {
    private String link;
    private String title; // the title tag
    private String description; // the description tag
    private int status; // http status
    private MediaVO image; // og:image

    private long timestamp;

    public static @Nullable LinkPreviewVO from(@Nullable LinkPreview source) {
        if (source == null) return null;
        LinkPreviewVO vo = new LinkPreviewVO();
        vo.setLink(source.getLink());
        vo.setTitle(source.getTitle());
        vo.setStatus(source.getStatus());
        vo.setDescription(source.getDescription());
        if (source.getImage() != null) {
            vo.setImage(MediaVO.from(source.getImage()));
        }
        vo.setTimestamp(source.getTimestamp().getEpochSecond());
        return vo;
    }
}
