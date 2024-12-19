package org.qbynet.chat.entity;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class Status {
    @Id
    public String id;

    @DBRef
    private User user;

    private int status; // 0-none 1-text 2-play 3-listen

    @Nullable
    private String text;
}
