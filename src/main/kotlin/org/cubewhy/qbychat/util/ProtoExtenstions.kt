package org.cubewhy.qbychat.util

import com.google.protobuf.Timestamp
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