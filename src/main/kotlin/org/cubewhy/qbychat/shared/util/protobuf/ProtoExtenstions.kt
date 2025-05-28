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

package org.cubewhy.qbychat.shared.util.protobuf

import com.google.protobuf.*
import com.google.protobuf.Any
import org.cubewhy.qbychat.common.v1.id
import org.cubewhy.qbychat.rpc.federation.v1.federationId
import org.cubewhy.qbychat.rpc.protocol.v1.clientboundMessage
import java.time.Instant
import java.util.*

fun Date.toProtobufTimestamp(): Timestamp = timestamp {
    this.seconds = this@toProtobufTimestamp.time
    this.nanos = 0
}

fun Instant.toProtobufTimestamp(): Timestamp = timestamp {
    this.seconds = this@toProtobufTimestamp.epochSecond
    this.nanos = this@toProtobufTimestamp.nano
}

fun String.toLocalId() = id { stringId = this@toLocalId }
fun String.toFederationId(domain: String? = null) = federationId {
    domain?.let { this.domain = it }
    localId = this@toFederationId.toLocalId()
}

fun protobufEventOf(event: GeneratedMessage, userId: String?) = clientboundMessage {
    userId?.let { this.userId = it.toLocalId() }
    this.event = Any.pack(event)
}

inline fun <T : GeneratedMessage, B : GeneratedMessage.Builder<B>> newMessageBuilder(
    builder: () -> B,
    block: B.() -> Unit
): T {
    val builderInstance = builder()
    builderInstance.block()
    @Suppress("UNCHECKED_CAST")
    return builderInstance.build() as T
}

inline fun <T : Message, B : GeneratedMessage.Builder<B>> B.buildWith(block: B.() -> Unit): T {
    this.apply(block)
    @Suppress("UNCHECKED_CAST")
    return this.build() as T
}