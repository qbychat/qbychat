package org.qbynet.chat.entity.dto;

import lombok.Data;

@Data
public class RemoveAvatarDTO {
    /**
     * Avatar id
     */
    private String avatar;
    private String conversation; // the conversation id
    private String bot; // bot id
}
