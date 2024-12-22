package org.qbynet.chat.entity.dto;

import lombok.Data;

@Data
public class ConfigAutoDeleteTimerDTO {
    private String conversation; // conversation id
    private int duration; // set -1 to disable
}
