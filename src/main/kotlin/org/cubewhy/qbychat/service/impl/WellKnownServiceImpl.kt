package org.cubewhy.qbychat.service.impl

import org.cubewhy.qbychat.entity.config.QbyChatProperties
import org.cubewhy.qbychat.entity.response.ClientDiscoveryResponse
import org.cubewhy.qbychat.service.WellKnownService
import org.springframework.stereotype.Service

@Service
class WellKnownServiceImpl(
    private val qbyChatProperties: QbyChatProperties
) : WellKnownService {
    override suspend fun client(baseUri: String): ClientDiscoveryResponse {
        return ClientDiscoveryResponse(
            homeserver = ClientDiscoveryResponse.Homeserver(
                websocketAddress = buildWebSocketUrl(baseUri, qbyChatProperties.websocket.path),
                requireEncryption = qbyChatProperties.websocket.requireEncryption
            )
        )
    }

    private fun buildWebSocketUrl(uri: String, wsPath: String): String {
        val normalizedUri = uri.trim().removeSuffix("/")
        val normalizedPath = wsPath.trim().removePrefix("/")

        return when {
            normalizedUri.startsWith("https://", ignoreCase = true) ->
                "wss://${normalizedUri.removePrefix("https://")}/$normalizedPath"

            normalizedUri.startsWith("http://", ignoreCase = true) ->
                "ws://${normalizedUri.removePrefix("http://")}/$normalizedPath"

            else ->
                throw IllegalArgumentException("Unsupported URI scheme: $uri")
        }
    }

}