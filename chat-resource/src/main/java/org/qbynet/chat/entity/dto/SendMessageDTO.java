package org.qbynet.chat.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class SendMessageDTO {
    private String conversation;
    private String content = null;
    private String sticker = null;

    private String replyTo = null; // message id
    private String redirectFrom = null; // message id

    private boolean linkPreview = true;

    private List<String> medias = List.of(); // media ids (should upload first)
}
