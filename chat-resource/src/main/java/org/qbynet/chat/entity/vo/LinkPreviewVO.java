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
    private MediaVO image;

    private long timestamp;

    public static @NotNull LinkPreviewVO from(@NotNull LinkPreview origin) {
        LinkPreviewVO lp = new LinkPreviewVO();
        lp.setLink(origin.getLink());
        lp.setTitle(origin.getTitle());
        lp.setDescription(origin.getDescription());
        lp.setImage(MediaVO.from(origin.getImage()));
        lp.setTimestamp(origin.getTimestamp().getEpochSecond());
        return lp;
    }
}
