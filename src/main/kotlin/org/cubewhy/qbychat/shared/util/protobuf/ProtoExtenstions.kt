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
import org.cubewhy.qbychat.websocket.protocol.v1.ClientboundMessage
import java.time.Instant
import java.util.*

fun Date.toProtobufType(): Timestamp = timestamp {
    this.seconds = this@toProtobufType.time
    this.nanos = 0
}

fun Instant.toProtobufType(): Timestamp = timestamp {
    this.seconds = this@toProtobufType.epochSecond
    this.nanos = this@toProtobufType.nano
}

fun protobufEventOf(event: GeneratedMessage, userId: String?): ClientboundMessage =
    ClientboundMessage.newBuilder().apply {
        userId?.let { this.userId = it }
        this.event = Any.pack(event)
    }.build()

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