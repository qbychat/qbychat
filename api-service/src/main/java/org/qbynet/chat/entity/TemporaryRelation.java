package org.qbynet.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash
public class TemporaryRelation {
    @Id
    private String id;

    @Indexed
    private String owner;
    private String target;

    @TimeToLive
    private Long expire;
}
