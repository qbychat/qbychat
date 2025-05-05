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
import org.cubewhy.qbychat.websocket.protocol.v1.ClientboundHandshake

data class WebsocketResponse(
    var response: GeneratedMessage? = null,
    var type: WebsocketResponseType = WebsocketResponseType.COMMON,
    var events: List<WebsocketEvent> = emptyList(),
) {
    var userId: String? = null
    var ticket: String? = null // request ticket
}

data class WebsocketEvent(
    val eventMessage: GeneratedMessage,
    val shared: Boolean // should this event shared over sessions that logged in the same account?
)


enum class WebsocketResponseType {
    COMMON,
    HANDSHAKE
}

fun sharedEventOf(vararg events: GeneratedMessage) = events.map { WebsocketEvent(it, true) }
fun sharedEventOf(events: List<GeneratedMessage>) = events.map { WebsocketEvent(it, true) }
fun eventOf(vararg events: GeneratedMessage) = events.map { WebsocketEvent(it, true) }
fun eventOf(events: List<GeneratedMessage>) = events.map { WebsocketEvent(it, true) }

fun websocketResponseOf(response: GeneratedMessage, events: List<WebsocketEvent> = listOf()): WebsocketResponse {
    return WebsocketResponse(response, events = events)
}

fun handshakeResponseOf(response: ClientboundHandshake) =
    WebsocketResponse(response, type = WebsocketResponseType.HANDSHAKE)

fun emptyWebsocketResponse() = WebsocketResponse()