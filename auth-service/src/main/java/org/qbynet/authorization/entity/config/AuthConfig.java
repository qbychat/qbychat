package org.qbynet.authorization.entity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "qbychat.auth")
public class AuthConfig {
    private List<String> scopes;
}
