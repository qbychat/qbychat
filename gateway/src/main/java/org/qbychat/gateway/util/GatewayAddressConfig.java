package org.qbychat.gateway.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qbychat.gateway.address")
public class GatewayAddressConfig {
    private String api;
    private String auth;
}
