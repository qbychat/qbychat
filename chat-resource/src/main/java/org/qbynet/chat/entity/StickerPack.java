package org.qbynet.chat.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class StickerPack {
    @Id
    private String id;

    private String name;
    private String title;
    @DBRef
    private User owner;

    @DBRef
    private Sticker thumbnail;

    private String telegramUpstream = null;

    private Instant timestamp = Instant.now(); // create at

    public boolean isBelongsTo(@NotNull User user) {
        if (this.owner == null) return false;
        return this.owner.getId().equals(user.getId());
    }
}
