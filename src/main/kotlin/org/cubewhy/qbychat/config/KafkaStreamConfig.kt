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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.qbychat.avro.FederationMessage
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.util.sendWithEncryption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer


@Configuration
class KafkaStreamConfig(
    private val scope: CoroutineScope,
) {

    @Bean
    fun qbychatWebsocketPayloadConsumer(sessionService: SessionService): Consumer<FederationMessage> {
        return Consumer { message ->
            scope.launch {
                sessionService.processWithSessionLocally(message.userId) { session ->
                    // push
                    session.sendWithEncryption(message.payload.array()).awaitFirstOrNull()
                }
            }
        }
    }
}