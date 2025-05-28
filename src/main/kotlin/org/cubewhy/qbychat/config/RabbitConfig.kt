/*
 *  Copyright (c) 2025. All rights reserved.
 *  This file is a part of the QbyChat project
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.cubewhy.qbychat.config

import com.rabbitmq.stream.Environment
import com.rabbitmq.stream.ProducerBuilder
import com.rabbitmq.stream.StreamCreator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate
import org.springframework.rabbit.stream.support.StreamAdmin
import java.time.Duration

@Configuration
class RabbitConfig {
    @Bean
    fun streamAdmin(env: Environment): StreamAdmin {
        return StreamAdmin(env) { sc: StreamCreator ->
            sc.stream("stream.qc.queue1").maxAge(Duration.ofMinutes(5)).create()
        }
    }

    @Bean
    fun streamTemplate(env: Environment): RabbitStreamTemplate {
        val template = RabbitStreamTemplate(env, "stream.qc.queue1")
        template.setProducerCustomizer { _: String?, builder: ProducerBuilder -> builder.name("data") }
        return template
    }
}