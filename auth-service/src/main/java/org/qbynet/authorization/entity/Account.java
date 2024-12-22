package org.qbynet.authorization.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document
public class Account {
    @Id
    private String id;

    private String password;

    private String email;

    private LocalDateTime registerDate = LocalDateTime.now();
    private List<Role> roles = new ArrayList<>(List.of(Role.USER));

    private LocalDateTime lockedExpiration = null;
    private boolean lockedPermanently = false;

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public boolean isLocked() {
        if (lockedPermanently) return true;
        if (lockedExpiration == null) {
            return false;
        }
        return lockedExpiration.isBefore(LocalDateTime.now());
    }
}
