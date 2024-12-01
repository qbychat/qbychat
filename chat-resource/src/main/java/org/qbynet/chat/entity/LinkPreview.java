package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class LinkPreview {
    @Id
    private String id;

    @Indexed(unique = true)
    private String link;
    private String title;
    private String description;
    private int status;
    @DBRef
    private Media image = null;

    private Instant timestamp = Instant.now();
}
