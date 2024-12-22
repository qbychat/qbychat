package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
public class BotUser {
    @Id
    private String id;

    @DBRef
    private Bot bot;
    @DBRef
    private User user;
}
