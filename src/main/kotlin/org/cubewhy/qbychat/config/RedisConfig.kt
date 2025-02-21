package org.cubewhy.qbychat.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.cubewhy.qbychat.entity.UserWebsocketSession
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun userSessionReactiveRedisTemplate(
        objectMapper: ObjectMapper,
        factory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, UserWebsocketSession> {
        val serializer = Jackson2JsonRedisSerializer(objectMapper, UserWebsocketSession::class.java)
        val context = RedisSerializationContext.newSerializationContext<String, UserWebsocketSession>(StringRedisSerializer())
            .value(serializer)
            .build()
        return ReactiveRedisTemplate(factory, context)
    }
}