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

package org.cubewhy.qbychat.application.service.v1

import org.cubewhy.qbychat.exception.RpcBadRequest
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection
import org.cubewhy.qbychat.rpc.session.v1.RegisterClientRequest
import org.cubewhy.qbychat.rpc.session.v1.ResumeClientRequest
import org.cubewhy.qbychat.shared.model.WebsocketResponse

interface SessionServiceV1 {
    /**
     * Registers a client to the current WebSocket session.
     *
     * Requirements:
     * - Can only be registered once;
     * - If the client is already registered, it returns an error;
     *
     * @param connection The current WebSocket session
     * @param payload The client registration request payload
     * @return The response after successful registration
     * @throws RpcBadRequest If the client has already been registered
     */
    suspend fun registerClient(connection: ClientConnection<*>, payload: RegisterClientRequest): WebsocketResponse
    suspend fun resumeClient(connection: ClientConnection<*>, payload: ResumeClientRequest): WebsocketResponse
}