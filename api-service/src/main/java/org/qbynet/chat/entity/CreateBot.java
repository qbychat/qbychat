package org.qbynet.chat.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateBot {
    private Bot bot;
    private String token; // unencrypted token
}
