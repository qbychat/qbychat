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

package org.cubewhy.qbychat.service.v1.impl

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.qbychat.entity.Client
import org.cubewhy.qbychat.entity.ClientMetadata
import org.cubewhy.qbychat.entity.WebsocketResponse
import org.cubewhy.qbychat.entity.websocketResponseOf
import org.cubewhy.qbychat.exception.WebsocketBadRequest
import org.cubewhy.qbychat.exception.WebsocketForbidden
import org.cubewhy.qbychat.exception.WebsocketUnauthorized
import org.cubewhy.qbychat.repository.ClientRepository
import org.cubewhy.qbychat.repository.SessionRepository
import org.cubewhy.qbychat.service.SessionManager
import org.cubewhy.qbychat.service.v1.SessionServiceV1
import org.cubewhy.qbychat.util.clientId
import org.cubewhy.qbychat.util.generateSecureSecret
import org.cubewhy.qbychat.websocket.session.v1.RegisterClientRequest
import org.cubewhy.qbychat.websocket.session.v1.RegisterClientResponse
import org.cubewhy.qbychat.websocket.session.v1.ResumeClientRequest
import org.cubewhy.qbychat.websocket.session.v1.ResumeClientResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class SessionServiceV1Impl(
    private val sessionManager: SessionManager,
    private val clientRepository: ClientRepository,
    private val passwordEncoder: PasswordEncoder,
    private val sessionRepository: SessionRepository
) : SessionServiceV1 {
    override suspend fun registerClient(
        session: WebSocketSession,
        payload: RegisterClientRequest
    ): WebsocketResponse {
        val clientMetadata = ClientMetadata(
            name = payload.clientMetadata.clientName,
            version = payload.clientMetadata.clientVersion,
            platform = ClientMetadata.Platform.fromProtobuf(payload.clientMetadata.platform)
        )
        // generate base token
        val authToken = generateSecureSecret(byteLength = 32)

        val client = clientRepository.save(
            Client(
                metadata = clientMetadata,
                authToken = passwordEncoder.encode(authToken)
            )
        ).awaitFirst()
        session.clientId = client.id!!
        // join token
        val finalToken = "${client.id}:$authToken"
        return websocketResponseOf(RegisterClientResponse.newBuilder().apply {
            this.token = finalToken
        }.build())
    }

    override suspend fun resumeClient(
        session: WebSocketSession,
        payload: ResumeClientRequest
    ): WebsocketResponse {
        val token = payload.token
        // Check if the token format is correct (e.g., "clientId:authToken")
        val tokenParts = token.split(":")
        if (tokenParts.size != 2) {
            throw WebsocketBadRequest("Invalid token format. Expected 'clientId:authToken'.")
        }

        val clientId = tokenParts[0]
        val providedAuthToken = tokenParts[1]

        if (sessionManager.isClientOnline(clientId)) {
            throw WebsocketForbidden("This client is current online, you cannot login using the same token.")
        }

        // Retrieve the client from the repository using clientId
        val client = clientRepository.findById(clientId).awaitFirstOrNull()
            ?: throw WebsocketUnauthorized("Client not found or token is invalid.")

        // Verify the provided token against the stored token (passwordEncoder will handle encoding checks)
        val isTokenValid = passwordEncoder.matches(providedAuthToken, client.authToken)
        if (!isTokenValid) {
            throw WebsocketUnauthorized("Invalid token.")
        }

        session.clientId = client.id!!

        // find accounts
        val accounts = sessionRepository.findAllByClientId(clientId).map { it.userId }.collectList().awaitFirst()
        // find main session
        val mainSession = client.mainSessionId.takeIf { it != null }?.let {  sessionRepository.findById(it).awaitFirstOrNull() }

        return websocketResponseOf(ResumeClientResponse.newBuilder().apply {
            this.addAllAccountIds(accounts)
            mainSession?.let { this.currentAccountId = it.userId }

        }.build())
    }
}