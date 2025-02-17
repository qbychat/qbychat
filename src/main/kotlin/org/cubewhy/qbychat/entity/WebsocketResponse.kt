package org.cubewhy.celestial.entity

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage

data class WebsocketResponse(
    var response: GeneratedMessage? = null,
    var events: List<GeneratedMessage> = emptyList(),
) {
    var requestId: ByteString? = null

    companion object {
        fun create(response: GeneratedMessage?): WebsocketResponse? {
            if (response == null) return null
            return WebsocketResponse(response)
        }

        fun create(response: GeneratedMessage, vararg events: GeneratedMessage): WebsocketResponse {
            return WebsocketResponse(response, events.asList())
        }

        fun create(response: GeneratedMessage, events: List<GeneratedMessage>): WebsocketResponse {
            return WebsocketResponse(response, events)
        }
    }
}

fun GeneratedMessage.toWebsocketResponse() =
    WebsocketResponse.create(this)

fun emptyWebsocketResponse() = WebsocketResponse()