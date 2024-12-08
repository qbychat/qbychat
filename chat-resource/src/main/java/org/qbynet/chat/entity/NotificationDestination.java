package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Data
@RedisHash(timeToLive = 3600L)
public class NotificationDestination {
    @Id
    private String id;

    @Indexed
    private String user; // user id
    private String notification; // notification id
}
