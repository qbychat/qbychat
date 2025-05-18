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

package org.cubewhy.qbychat.application.service.impl

import org.cubewhy.qbychat.application.service.WellKnownService
import org.cubewhy.qbychat.config.properties.QbyChatProperties
import org.cubewhy.qbychat.interfaces.controller.response.ClientDiscoveryResponse
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