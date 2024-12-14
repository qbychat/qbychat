package org.qbychat.gateway.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceConfig {
    private String api;
    private String auth;
}
