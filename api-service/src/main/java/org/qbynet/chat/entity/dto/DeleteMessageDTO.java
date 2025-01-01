package org.qbynet.chat.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeleteMessageDTO {
    private List<String> messages;
}
