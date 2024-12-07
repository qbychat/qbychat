package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class JoinRequest {
    @Id
    private String id;

    @DBRef
    private User user;
    @DBRef
    private Conversation conversation;

    private Instant timestamp = Instant.now();
}
