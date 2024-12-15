package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class StickerPack {
    @Id
    private String id;

    private String name;
    @DBRef
    private User owner;
    private String link; // http://resource-server/view/sticker/<link>
}
