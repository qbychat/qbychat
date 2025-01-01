package org.qbynet.chat.entity.dto;

import lombok.Data;

@Data
public class ConfigAnonymousDTO {
    private String conversation;
    private String user;
    private boolean state;
}
