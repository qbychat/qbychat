package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class User {
    @Id
    private String id; // http://resource-server/view/u/<id>/id
    private String remoteId; // id from OAuth2 Authorization server

    private String username = null; // the name for user details link http://resource-server/view/u/<username>
    private String nickname = ""; // the name for everyone
    private String bio = null; // a short text to describe yourself

    private Instant registerTime = Instant.now();
    private Instant lastLoginTime = null;
}
