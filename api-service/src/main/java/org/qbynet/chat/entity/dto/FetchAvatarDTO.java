package org.qbynet.chat.entity.dto;

import lombok.Data;

@Data
public class FetchAvatarDTO {
    private String userId;
    private String conversationId;
}
