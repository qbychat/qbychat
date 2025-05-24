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

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.qbychat.application.service.SessionManager
import org.cubewhy.qbychat.application.service.v1.UserServiceV1
import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.domain.repository.UserRepository
import org.cubewhy.qbychat.infrastructure.transport.ClientConnection
import org.cubewhy.qbychat.shared.util.protobuf.RegisterAccountResponsesV1
import org.cubewhy.qbychat.websocket.user.v1.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserServiceV1Impl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val sessionManager: SessionManager
) : UserServiceV1 {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val usernameRegex = "^[a-zA-Z0-9]{5,16}$".toRegex()

    override suspend fun sync(request: SyncRequest, user: User): SyncResponse {
        return syncResponse {
            publicInfo = publicUserInfo {
                username = user.username
                nickname = user.nickname
                bio = user.bio
            }
        }
    }


    override suspend fun registerAccount(
        request: RegisterAccountRequest,
        connection: ClientConnection<*>
    ): RegisterAccountResponse {
        // check if username available
        if (userRepository.existsByUsernameIgnoreCase(request.username).awaitFirst()) {
            return RegisterAccountResponsesV1.usernameExists()
        }
        if (!usernameRegex.matches(request.username)) {
            return RegisterAccountResponsesV1.badUsername()
        }

        // create user
        val user = userRepository.save(
            User(
                username = request.username,
                password = passwordEncoder.encode(request.password),
                nickname = request.username,
            )
        ).awaitFirst()
        logger.info { "Created user ${user.username}." }
        // add user to session
        sessionManager.createSession(user, connection)
        return RegisterAccountResponsesV1.success(user.id!!)
    }
}