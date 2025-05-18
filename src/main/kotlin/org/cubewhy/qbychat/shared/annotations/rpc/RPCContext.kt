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

package org.cubewhy.qbychat.shared.annotations.rpc

import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection

data class RPCContext(
    val user: User?,
    val connection: ClientConnection<*>,
    val payload: ByteArray?,
    val rawArgs: List<Any?> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RPCContext

        if (user != other.user) return false
        if (connection != other.connection) return false
        if (!payload.contentEquals(other.payload)) return false
        if (rawArgs != other.rawArgs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user?.hashCode() ?: 0
        result = 31 * result + connection.hashCode()
        result = 31 * result + (payload?.contentHashCode() ?: 0)
        result = 31 * result + rawArgs.hashCode()
        return result
    }
}
