package org.qbynet.chat.entity.dto;

import lombok.Data;

@Data
public class RemoveConversationAvatarDTO {
    private String conversation;
    private String avatar;
}
