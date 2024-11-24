package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Sticker {
    @Id
    private String id;

    private String alternativeEmoji;
    @DBRef
    private Media media; // link to a lottie json, image or gif file
}
