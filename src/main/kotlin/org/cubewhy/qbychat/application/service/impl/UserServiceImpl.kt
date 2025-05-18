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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.qbychat.application.service.SessionManager
import org.cubewhy.qbychat.application.service.UserService
import org.cubewhy.qbychat.domain.model.User
import org.cubewhy.qbychat.domain.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class UserServiceImpl(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : UserService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        return userRepository.findByUsernameIgnoreCase(username)
            .flatMap { user ->
                // build User details
                org.springframework.security.core.userdetails.User.builder()
                    .username(user.username)
                    .password(user.password)
                    .roles(user.roles.toString())
                    .build().toMono()
            }
    }

    override fun loadUserByUsername(username: String): Mono<User> {
        return userRepository.findByUsernameIgnoreCase(username)
    }
}

