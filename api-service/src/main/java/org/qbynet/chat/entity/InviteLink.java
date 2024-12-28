package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class InviteLink {
    @Id
    private String id;

    @DBRef
    private Member owner;

    @Indexed(unique = true)
    private String link;

    private Instant expireAt;
}
