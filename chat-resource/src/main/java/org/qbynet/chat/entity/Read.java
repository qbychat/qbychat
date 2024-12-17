package org.qbynet.chat.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
@Builder
public class Read {
    @Id
    private String id;

    @DBRef
    private Message message;
    @DBRef
    private Member member;
    private Instant timestamp = Instant.now();
}
