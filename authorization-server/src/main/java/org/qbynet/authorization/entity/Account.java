package org.qbynet.authorization.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Document
public class Account implements UserDetails {
    @Id
    private String id;

    private String username;
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(it -> new SimpleGrantedAuthority(it.name())).toList();
    }
}
