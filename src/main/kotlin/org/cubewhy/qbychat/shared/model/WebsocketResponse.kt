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

package org.cubewhy.qbychat.shared.model

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.kotlin.toByteString
import org.cubewhy.qbychat.rpc.protocol.v1.ClientboundHandshake
import org.cubewhy.qbychat.rpc.protocol.v1.RpcResponse
import org.cubewhy.qbychat.rpc.protocol.v1.clientboundMessage
import org.cubewhy.qbychat.rpc.protocol.v1.rpcResponse
import org.cubewhy.qbychat.shared.util.protobuf.toLocalId

data class WebsocketResponse(
    val payload: ByteArray?,
    val status: RpcResponse.Status = RpcResponse.Status.STATUS_SUCCESS,
    val message: String? = null,
    var events: List<WebsocketEvent> = emptyList(),
) {
    var clientboundHandshake: ClientboundHandshake? = null
    var userId: String? = null
    var ticket: ByteArray? = null

    fun buildRpcResponse() = rpcResponse {
        ticket = this@WebsocketResponse.ticket!!.toByteString()
        this@WebsocketResponse.payload?.let { payload = it.toByteString() }
        status = this@WebsocketResponse.status
        this@WebsocketResponse.message?.let { message = it }
    }

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
) {
    fun buildProtobufMessage(userId: String?) = clientboundMessage {
        userId?.let { this.userId = it.toLocalId() }
        this.event = com.google.protobuf.Any.pack(eventMessage)
    }
}

fun sharedEventOf(vararg events: GeneratedMessage) = events.map { WebsocketEvent(it, true) }
fun sharedEventOf(events: List<GeneratedMessage>) = events.map { WebsocketEvent(it, true) }
fun eventOf(vararg events: GeneratedMessage) = events.map { WebsocketEvent(it, true) }
fun eventOf(events: List<GeneratedMessage>) = events.map { WebsocketEvent(it, true) }

fun websocketResponseOf(
    payload: ByteArray?,
    status: RpcResponse.Status = RpcResponse.Status.STATUS_SUCCESS,
    message: String? = null,
    events: List<WebsocketEvent> = listOf()
): WebsocketResponse {
    return WebsocketResponse(payload, status, message, events = events)
}

fun websocketResponseOf(
    payload: GeneratedMessage,
    events: List<WebsocketEvent> = listOf()
): WebsocketResponse {
    return WebsocketResponse(payload.toByteArray(), RpcResponse.Status.STATUS_SUCCESS, null, events = events)
}

fun errorWebsocketResponseOf(
    status: RpcResponse.Status,
    message: String?
) = websocketResponseOf(null, status, message)
