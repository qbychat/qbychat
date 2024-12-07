package org.qbynet.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.LinkPreview;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkPreviewVO {
    private String link;
    private String title;
    private String description;
    private int status;
    private MediaVO image;

    private long timestamp;

    public static @NotNull LinkPreviewVO from(@NotNull LinkPreview source) {
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
