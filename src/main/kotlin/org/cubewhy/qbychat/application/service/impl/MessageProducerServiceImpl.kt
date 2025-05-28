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

package org.cubewhy.qbychat.application.service.impl

import kotlinx.coroutines.future.await
import org.cubewhy.qbychat.application.service.MessageProducerService
import org.cubewhy.qbychat.common.v1.Id
import org.cubewhy.qbychat.rpc.protocol.v1.InstanceEventKt
import org.cubewhy.qbychat.rpc.protocol.v1.instanceEvent
import org.cubewhy.qbychat.shared.util.protobuf.toProtobufTimestamp
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MessageProducerServiceImpl(
    private val rabbitStreamTemplate: RabbitStreamTemplate,
) : MessageProducerService {

    override suspend fun sendEvent(
        userId: Id,
        builder: InstanceEventKt.Dsl.() -> Unit
    ): Boolean {
        val protoEvent = instanceEvent {
            this.userId = userId
            timestamp = Instant.now().toProtobufTimestamp()
            builder.invoke(this@instanceEvent)
        }
        return rabbitStreamTemplate.convertAndSend(protoEvent.toByteArray()).await()
    }
}