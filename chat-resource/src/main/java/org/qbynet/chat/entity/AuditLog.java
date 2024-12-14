package org.qbynet.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    @Id
    private String id;

    @DBRef
    private User operator;
    @DBRef
    private Conversation targetConversation;
    @DBRef
    private Message targetMessage;
    @DBRef
    private User targetUser;

    private AuditType type;
    private String description;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
