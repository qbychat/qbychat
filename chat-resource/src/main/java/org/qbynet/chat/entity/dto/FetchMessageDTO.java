package org.qbynet.chat.entity.dto;

import lombok.Data;

@Data
public class FetchMessageDTO {
    private String conversationId;
    private int size;
    private int page;
}
