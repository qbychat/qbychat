package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class InviteLink {
    @Id
    private String id;

    @DBRef
    private Member createBy;
    @DBRef
    private Conversation conversation;

    private String link;
}
