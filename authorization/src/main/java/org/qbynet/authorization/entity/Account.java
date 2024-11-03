package org.qbynet.authorization.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data
@Document
public class Account implements UserDetails, BaseData {
    @Id
    private String id;

    private String username;
    private String email;
    private String password;

    private Date lastLogin;
    private Date registerTime = new Date();

    private List<String> roles = List.of(Role.USER);

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }
}
