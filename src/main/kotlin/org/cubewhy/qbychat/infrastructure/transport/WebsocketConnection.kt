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

package org.cubewhy.qbychat.infrastructure.transport

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

class WebsocketConnection(override val nativeConnection: WebSocketSession) : ClientConnection<WebSocketSession>() {
    override val id: String
        get() = this.nativeConnection.id

    override val isOpen: Boolean
        get() = this.nativeConnection.isOpen

    override suspend fun send(message: ByteArray) {
        this.nativeConnection.send(this.nativeConnection.binaryMessage { it.wrap(message) }.toMono())
            .awaitFirstOrNull()
    }

    override suspend fun send(messages: List<ByteArray>) {
        this.nativeConnection.send(messages.map { message -> this.nativeConnection.binaryMessage { it.wrap(message) } }
            .toFlux())
            .awaitFirstOrNull()
    }


    override suspend fun close(code: Int, reason: String?) {
        this.nativeConnection.close(CloseStatus.create(code, reason)).awaitFirstOrNull()
    }
}