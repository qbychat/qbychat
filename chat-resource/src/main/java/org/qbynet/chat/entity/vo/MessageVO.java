package org.qbynet.chat.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessageVO {
    private String id;
    private MemberVO sender;
    private String content;

    private String reply;
    private String redirect;
    private LinkPreviewVO linkPreview;
    private String language;

    @Builder.Default
    private List<MediaVO> medias = List.of();

    private long sentAt;
    @Builder.Default
    private long editAt = -1L;
}
