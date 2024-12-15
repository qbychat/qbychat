package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document
public class Sticker implements Serializable {
    @Id
    private String id;

    private String emoji;
    @DBRef
    private Media media; // link to a lottie json, image or gif file

    @DBRef
    private StickerPack pack;
}
