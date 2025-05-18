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

import com.google.protobuf.Any
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Timestamp
import org.cubewhy.qbychat.websocket.protocol.v1.ClientboundMessage
import java.time.Instant
import java.util.*

fun Date.toProtobufType(): Timestamp = Timestamp.newBuilder().apply {
    this.seconds = this@toProtobufType.time
    this.nanos = 0
}.build()

fun Instant.toProtobufType(): Timestamp = Timestamp.newBuilder().apply {
    this.seconds = this@toProtobufType.epochSecond
    this.nanos = this@toProtobufType.nano
}.build()

fun protobufEventOf(event: GeneratedMessage, userId: String?): ClientboundMessage =
    ClientboundMessage.newBuilder().apply {
        userId?.let { this.userId = it }
        this.event = Any.pack(event)
    }.build()

