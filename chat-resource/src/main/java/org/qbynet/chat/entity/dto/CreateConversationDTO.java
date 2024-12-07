package org.qbynet.chat.entity.dto;

import lombok.Data;
import org.qbynet.chat.entity.ConversationType;

@Data
public class CreateConversationDTO {
    private String name;
    private ConversationType type;
}
