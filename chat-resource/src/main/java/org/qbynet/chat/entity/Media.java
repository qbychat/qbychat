package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Media {
    @Id
    private String id;

    private String name;
    private String hash;

    private String contentType;
}
