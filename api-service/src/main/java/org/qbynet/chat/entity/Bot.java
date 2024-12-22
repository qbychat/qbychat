package org.qbynet.chat.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Bot {
    @Id
    private String id;

    @DBRef
    private User bot;
    @DBRef
    private User owner;

    private String token; // encrypted token, set to null = internal bot account

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isBelongTo(@NotNull User user) {
        return this.getOwner().getId().equals(user.getId());
    }
}
