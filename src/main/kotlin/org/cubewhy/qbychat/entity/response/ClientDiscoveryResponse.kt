package org.cubewhy.qbychat.entity.response

import com.fasterxml.jackson.annotation.JsonProperty

data class ClientDiscoveryResponse(
    val homeserver: Homeserver
) {
    data class Homeserver(
        @JsonProperty("websocket_address")
        val websocketAddress: String,
        @JsonProperty("require_encryption")
        val requireEncryption: Boolean
    )
}
