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

package org.cubewhy.qbychat.application.service.v1.impl

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.qbychat.application.service.SessionManager
import org.cubewhy.qbychat.application.service.v1.SessionServiceV1
import org.cubewhy.qbychat.domain.model.Client
import org.cubewhy.qbychat.domain.model.ClientMetadata
import org.cubewhy.qbychat.domain.repository.ClientRepository
import org.cubewhy.qbychat.domain.repository.SessionRepository
import org.cubewhy.qbychat.exception.WebsocketBadRequest
import org.cubewhy.qbychat.exception.WebsocketForbidden
import org.cubewhy.qbychat.exception.WebsocketUnauthorized
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection
import org.cubewhy.qbychat.shared.model.WebsocketResponse
import org.cubewhy.qbychat.shared.model.websocketResponseOf
import org.cubewhy.qbychat.shared.util.generateSecureSecret
import org.cubewhy.qbychat.websocket.session.v1.RegisterClientRequest
import org.cubewhy.qbychat.websocket.session.v1.ResumeClientRequest
import org.cubewhy.qbychat.websocket.session.v1.registerClientResponse
import org.cubewhy.qbychat.websocket.session.v1.resumeClientResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class SessionServiceV1Impl(
    private val sessionManager: SessionManager,
    private val clientRepository: ClientRepository,
    private val passwordEncoder: PasswordEncoder,
    private val sessionRepository: SessionRepository
) : SessionServiceV1 {
    override suspend fun registerClient(
        connection: ClientConnection<*>,
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
        // join token
        connection.metadata.clientId = client.id

        val finalToken = "${client.id}:$authToken"
        return websocketResponseOf(registerClientResponse {
            this.token = finalToken
        })
    }

    override suspend fun resumeClient(
        connection: ClientConnection<*>,
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

        connection.metadata.clientId = client.id!!

        // find accounts
        val sessions = sessionRepository.findAllByClientId(clientId)
        val accounts = sessions.map { it.userId }.collectList().awaitFirst()
        // find main session
        val mainSession =
            client.mainSessionId.takeIf { it != null }?.let { sessionRepository.findById(it).awaitFirstOrNull() }

        // register sessions
        accounts.forEach { account ->
            sessionManager.saveSession(connection, userId = account)
        }

        return websocketResponseOf(resumeClientResponse {
            accountIds.addAll(accounts)
            mainSession?.let { this.currentAccountId = it.userId }

        })
    }
}