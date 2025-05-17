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

package org.cubewhy.qbychat.entity

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.kotlin.toByteString
import org.cubewhy.qbychat.websocket.protocol.v1.ClientboundHandshake
import org.cubewhy.qbychat.websocket.protocol.v1.RPCResponse

data class WebsocketResponse(
    val payload: ByteArray?,
    val status: RPCResponse.Status = RPCResponse.Status.SUCCESS,
    val message: String? = null,
    var events: List<WebsocketEvent> = emptyList(),
) {
    var clientboundHandshake: ClientboundHandshake? = null
    var userId: String? = null
    var ticket: ByteArray? = null

    fun buildRPCResponse() = RPCResponse.newBuilder().apply {
        this.ticket = this@WebsocketResponse.ticket!!.toByteString()
        this@WebsocketResponse.payload?.let { this.payload = it.toByteString() }
        this.status = this@WebsocketResponse.status
        this@WebsocketResponse.message?.let { this.message = it }
    }.build()!!

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WebsocketResponse

        if (!payload.contentEquals(other.payload)) return false
        if (status != other.status) return false
        if (message != other.message) return false
        if (events != other.events) return false
        if (clientboundHandshake != other.clientboundHandshake) return false
        if (userId != other.userId) return false
        if (!ticket.contentEquals(other.ticket)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = payload?.contentHashCode() ?: 0
        result = 31 * result + status.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + events.hashCode()
        result = 31 * result + (clientboundHandshake?.hashCode() ?: 0)
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + (ticket?.contentHashCode() ?: 0)
        return result
    }


}

data class WebsocketEvent(
    val eventMessage: GeneratedMessage,
    val shared: Boolean // should this event shared over sessions that logged in the same account?
)

fun sharedEventOf(vararg events: GeneratedMessage) = events.map { WebsocketEvent(it, true) }
fun sharedEventOf(events: List<GeneratedMessage>) = events.map { WebsocketEvent(it, true) }
fun eventOf(vararg events: GeneratedMessage) = events.map { WebsocketEvent(it, true) }
fun eventOf(events: List<GeneratedMessage>) = events.map { WebsocketEvent(it, true) }

fun websocketResponseOf(
    payload: ByteArray?,
    status: RPCResponse.Status = RPCResponse.Status.SUCCESS,
    message: String? = null,
    events: List<WebsocketEvent> = listOf()
): WebsocketResponse {
    return WebsocketResponse(payload, status, message, events = events)
}

fun websocketResponseOf(
    payload: GeneratedMessage,
    events: List<WebsocketEvent> = listOf()
): WebsocketResponse {
    return WebsocketResponse(payload.toByteArray(), RPCResponse.Status.SUCCESS, null, events = events)
}

fun errorWebsocketResponseOf(
    status: RPCResponse.Status,
    message: String?
) = websocketResponseOf(null, status, message)
