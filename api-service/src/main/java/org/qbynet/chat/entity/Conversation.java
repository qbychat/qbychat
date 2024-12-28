package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Document
public class Conversation implements Serializable {
    @Id
    private String id;

    private String name;
    private String description;
    private ConversationType type;
    @Indexed(unique = true)
    private String link;
    private Instant createdAt = Instant.now();

    private List<MemberPermission> defaultPermissions = List.of(MemberPermission.MEMBER_DEFAULT);
    private boolean verifyNeeded = false;
    private boolean preview = true; // can member view messages without join?
    private boolean noForward = false; // can members forward messages? (include admins)

    private int autoDeleteTimer = -1; // How long do we delete all messages? (Unit: days)

    @DBRef
    private StickerPack stickerPack = null; // bound sticker pack

    public List<MemberPermission> getDefaultPermissions() {
        return MemberPermission.calculate(defaultPermissions);
    }
}
