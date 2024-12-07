package org.qbynet.chat.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReadMessageDTO {
    private List<String> messages; // message ids
}
