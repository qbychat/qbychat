package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class LinkPreview {
    @Id
    private String id;

    private String link;
    private String title;
    private String description;
    @DBRef
    private Media icon = null;

    private Instant timestamp = Instant.now();
}
