package org.cubewhy.qbychat.entity

import com.google.protobuf.GeneratedMessage
import org.cubewhy.qbychat.websocket.protocol.Protocol

data class WebsocketResponse(
    val userId: String? = null,
    var response: GeneratedMessage? = null,
    var type: WebsocketResponseType = WebsocketResponseType.COMMON,
    var events: List<GeneratedMessage> = emptyList(),
) {
    var ticket: String? = null // request ticket
}


enum class WebsocketResponseType {
    COMMON,
    HANDSHAKE
}

fun websocketResponse(userId: String, response: GeneratedMessage?): WebsocketResponse? {
    if (response == null) return null
    return WebsocketResponse(userId, response)
}

fun websocketResponse(userId: String, response: GeneratedMessage, vararg events: GeneratedMessage): WebsocketResponse {
    return WebsocketResponse(userId, response, events = events.asList())
}

fun websocketResponse(userId: String, response: GeneratedMessage, events: List<GeneratedMessage>): WebsocketResponse {
    return WebsocketResponse(userId, response, events = events)
}

fun handshakeResponse(response: Protocol.ServerHandshake) =
    WebsocketResponse(null, response, type = WebsocketResponseType.HANDSHAKE)

fun GeneratedMessage.toWebsocketResponse(userId: String) =
    WebsocketResponse(userId, this)

fun emptyWebsocketResponse() = WebsocketResponse()