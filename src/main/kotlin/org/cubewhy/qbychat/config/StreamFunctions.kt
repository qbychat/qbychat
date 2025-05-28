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

import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.application.service.InstanceEventService
import org.cubewhy.qbychat.rpc.protocol.v1.InstanceEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Function
import java.util.function.Supplier

@Configuration
class StreamFunctions(
    private val instanceEventService: InstanceEventService,
) {
    @Bean
    fun userEventSupplier(): Supplier<Flux<ByteArray>> {
        return Supplier { Flux.empty() }
    }

    @Bean
    fun userEventProcessor(): Function<Flux<ByteArray>, Mono<Void>> {
        return Function { input ->
            // TODO batch events
            input
                .mapNotNull { bytes -> parseProtobufMessage(bytes) }
                .cast(InstanceEvent::class.java)
                .filterWhen { event -> mono { instanceEventService.shouldProcessEvent(event) } }
                .groupBy { it.userId }
                .flatMap { groupedFlux ->
                    groupedFlux
                        .flatMap { event -> mono { instanceEventService.processEvent(event) } }
                }
                .then()
        }
    }

    private fun parseProtobufMessage(bytes: ByteArray): InstanceEvent? {
        return try {
            InstanceEvent.parseFrom(bytes)
        } catch (_: Exception) {
            null
        }
    }
}