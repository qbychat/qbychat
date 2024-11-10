package org.qbynet.authorization.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash(timeToLive = 604_800)
@Data
public class Verify {
    @Id
    private String id;
    @Indexed
    private String token;

    @Indexed
    private String username;
    private String password;
    @Indexed
    private String email;
}
