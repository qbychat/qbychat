/*
 * Copyright (c) 2025. All rights reserved.
 *
 * This file is a part of the QbyChat project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

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
        val context =
            RedisSerializationContext.newSerializationContext<String, UserWebsocketSession>(StringRedisSerializer())
                .value(serializer)
                .build()
        return ReactiveRedisTemplate(factory, context)
    }
}