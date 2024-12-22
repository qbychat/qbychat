package org.qbynet.chat.entity.dto;

import lombok.Data;

@Data
public class EditProfileDTO {
    private String username = null;
    private String nickname = "";
    private String bio = null;
}
