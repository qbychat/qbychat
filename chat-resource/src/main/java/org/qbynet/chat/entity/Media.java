package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Media {
    @Id
    private String id;

    /**
     * Upload user
     */
    @DBRef
    private User uploader = null; // null = create by system
    /**
     * File name
     */
    private String name;
    /**
     * Sha256 hash
     */
    private String hash;
    /**
     * Mime type
     */
    private String contentType;
}
