package org.qbynet.chat.module;

import org.qbynet.chat.entity.Privacy;

import java.io.Serializable;
import java.security.Principal;
import java.time.Instant;

public record User(String id, String remoteId, String username, String nickname, String bio) implements Principal, Serializable {

    @Override
    public String getName() {
        return id;
    }
}
