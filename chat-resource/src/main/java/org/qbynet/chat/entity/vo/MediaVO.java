package org.qbynet.chat.entity.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaVO {
    private String id;

    private String name;
    private String hash;
    private String contentType;
}
