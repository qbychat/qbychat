package org.qbynet.chat.entity;

import lombok.Data;
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
    private Member sender; // set to null to send messages anonymous
    @DBRef
    private Conversation conversation;
    private String content;
    @DBRef
    private Sticker sticker;

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

    private MessageType type = MessageType.TEXT_MESSAGE;

    private Instant expiresAt = null; // set to Instant.now() to delete message
    private boolean encrypted = false;
}
