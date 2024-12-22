package org.qbynet.chat.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class EditMessageDTO {
    private String message; // message id

    private String content; // new content
    private String sticker; // new sticker
    private boolean linkPreview;
    private List<String> medias;
}
