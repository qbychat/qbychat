package org.cubewhy.qbychat.entity

import com.google.protobuf.GeneratedMessage
import org.cubewhy.qbychat.websocket.protocol.Protocol

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

fun handshakeResponseOf(response: Protocol.ServerHandshake) =
    WebsocketResponse(response, type = WebsocketResponseType.HANDSHAKE)

fun emptyWebsocketResponse() = WebsocketResponse()