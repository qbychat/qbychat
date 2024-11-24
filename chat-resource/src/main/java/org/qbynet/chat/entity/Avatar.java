package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Avatar {
    @Id
    private String id;

    @DBRef
    private User user;
    @DBRef
    private Media media;
}
