package org.cubewhy.qbychat.util

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