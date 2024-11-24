package org.qbynet.chat.entity.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkPreviewVO {
    private String link;
    private String title;
    private String description;
    private MediaVO image;

    private long timestamp;
}
