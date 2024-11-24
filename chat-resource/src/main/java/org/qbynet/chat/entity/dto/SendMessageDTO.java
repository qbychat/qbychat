package org.qbynet.chat.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class SendMessageDTO {
    private String conversationId;
    private String content;

    private String replyTo; // message id
    private String redirectFrom; // message id

    private List<String> medias = List.of(); // media ids (should upload first)
}
