package org.qbynet.chat.entity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qbychat.telegram")
public class TelegramConfig {
    private boolean enabled;
    private String token;
}
