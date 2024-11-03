package org.qbynet.authorization.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizeVO {
    private String username;
    private List<String> roles;
    private String token;
    private String email;
    private long expire;
}
