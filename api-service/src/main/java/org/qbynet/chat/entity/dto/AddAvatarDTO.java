package org.qbynet.chat.entity.dto;

import lombok.Data;

@Data
public class AddAvatarDTO {
    private String media; // media id
    private String conversation; // the conversation id
    private String bot; // bot id
}
