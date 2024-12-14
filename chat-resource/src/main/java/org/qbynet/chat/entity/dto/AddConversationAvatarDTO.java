package org.qbynet.chat.entity.dto;

import lombok.Data;

@Data
public class AddConversationAvatarDTO {
    private String conversation; // the conversation id
    private String media; // the avatar media id
}
