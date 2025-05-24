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

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.qbychat.application.service.SessionManager
import org.cubewhy.qbychat.application.service.v1.AuthServiceV1
import org.cubewhy.qbychat.domain.repository.UserRepository
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection
import org.cubewhy.qbychat.shared.util.protobuf.UsernamePasswordLoginResponsesV1
import org.cubewhy.qbychat.websocket.auth.v1.UsernamePasswordLoginRequest
import org.cubewhy.qbychat.websocket.auth.v1.UsernamePasswordLoginResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthServiceV1Impl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val sessionManager: SessionManager
) : AuthServiceV1 {
    override suspend fun usernamePasswordLogin(
        connection: ClientConnection<*>,
        request: UsernamePasswordLoginRequest
    ): UsernamePasswordLoginResponse {
        // find user
        val user = userRepository.findByUsernameIgnoreCase(request.username).awaitFirstOrNull()
        if (user == null) return UsernamePasswordLoginResponsesV1.badUsernameOrPassword()
        // verify password
        if (!passwordEncoder.matches(
                request.password,
                user.password
            )
        ) return UsernamePasswordLoginResponsesV1.badUsernameOrPassword()

        // put user to session
        sessionManager.persistSession(user, connection)
        return UsernamePasswordLoginResponsesV1.success(user.id!!)
    }
}