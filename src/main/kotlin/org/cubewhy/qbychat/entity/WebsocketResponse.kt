package org.cubewhy.qbychat.entity

import com.google.protobuf.GeneratedMessage
import org.cubewhy.qbychat.websocket.protocol.Protocol

data class WebsocketResponse(
    var response: GeneratedMessage? = null,
    var type: WebsocketResponseType = WebsocketResponseType.COMMON,
    var events: List<GeneratedMessage> = emptyList(),
) {
    var userId: String? = null
    var ticket: String? = null // request ticket
}


enum class WebsocketResponseType {
    COMMON,
    HANDSHAKE
}

fun websocketResponse(response: GeneratedMessage, vararg events: GeneratedMessage): WebsocketResponse {
    return WebsocketResponse(response, events = events.asList())
}

fun websocketResponse(response: GeneratedMessage, events: List<GeneratedMessage>): WebsocketResponse {
    return WebsocketResponse(response, events = events)
}

fun handshakeResponse(response: Protocol.ServerHandshake) =
    WebsocketResponse(response, type = WebsocketResponseType.HANDSHAKE)

fun emptyWebsocketResponse() = WebsocketResponse()