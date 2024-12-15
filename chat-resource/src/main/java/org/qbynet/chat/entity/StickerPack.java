package org.qbynet.chat.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

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

    public boolean isBelongsTo(@NotNull User user) {
        if (this.owner == null) return false;
        return this.owner.getId().equals(user.getId());
    }
}
