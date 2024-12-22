package org.qbynet.chat.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Document
public class Message implements Serializable {
    @Id
    private String id; // http://resource-server/view/m/<id>

    @DBRef
    private Member sender;
    @DBRef
    private Conversation conversation;
    private String content = null;
    @DBRef
    private Sticker sticker = null;

    @DBRef
    private Message reply;
    @DBRef
    private Message redirect;
    @DBRef
    private LinkPreview linkPreview;
    private String language;

    @DBRef
    private List<Media> medias = List.of();

    private Instant sentAt = Instant.now();
    private Instant editAt = null; // null = never edited

    private MessageType type = MessageType.NORMAL_MESSAGE;

    private Instant expiresAt = null; // set to Instant.now() to delete message
    private boolean pinned = false;

    private boolean anonymous = false;

    public boolean isBelongsTo(@NotNull User user) {
        return this.sender.getUser().equals(user);
    }
}
