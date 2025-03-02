package org.cubewhy.qbychat.util

import com.google.protobuf.Any
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Timestamp
import org.cubewhy.qbychat.websocket.protocol.Protocol
import java.time.Instant
import java.util.Date

fun Date.toProtobufType(): Timestamp = Timestamp.newBuilder().apply {
    this.seconds = this@toProtobufType.time
    this.nanos = 0
}.build()

fun Instant.toProtobufType(): Timestamp = Timestamp.newBuilder().apply {
    this.seconds = this@toProtobufType.epochSecond
    this.nanos = this@toProtobufType.nano
}.build()

fun protobufEventOf(event: GeneratedMessage, userId: String?): Protocol.ClientboundMessage = Protocol.ClientboundMessage.newBuilder().apply {
    userId?.let { this.account = it }
    this.event = Any.pack(event)
}.build()