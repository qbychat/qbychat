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

package org.cubewhy.qbychat.annotations.rpc

import org.cubewhy.qbychat.entity.User
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import kotlin.reflect.KParameter

interface RPCArgumentResolver {
    fun supportsKParameter(param: KParameter): Boolean
    fun resolveKArgument(param: KParameter, context: RPCContext): Any?

    fun supportsParameter(param: java.lang.reflect.Parameter): Boolean
    fun resolveArgument(param: java.lang.reflect.Parameter, context: RPCContext): Any?
}

@Component
class UserArgumentResolver : RPCArgumentResolver {

    override fun supportsParameter(param: java.lang.reflect.Parameter): Boolean {
        return param.type == User::class.java
    }

    override fun supportsKParameter(param: KParameter): Boolean {
        return param.type.classifier == User::class
    }

    override fun resolveArgument(param: java.lang.reflect.Parameter, context: RPCContext): Any? {
        return context.user ?: throw IllegalStateException("User not found in context")
    }

    override fun resolveKArgument(param: KParameter, context: RPCContext): Any? {
        return context.user ?: throw IllegalStateException("User not found in context")
    }
}

@Component
class SessionArgumentResolver : RPCArgumentResolver {

    override fun supportsParameter(param: java.lang.reflect.Parameter): Boolean {
        return param.type == WebSocketSession::class.java
    }

    override fun supportsKParameter(param: KParameter): Boolean {
        return param.type.classifier == WebSocketSession::class
    }

    override fun resolveArgument(param: java.lang.reflect.Parameter, context: RPCContext): Any? {
        return context.session
    }

    override fun resolveKArgument(param: KParameter, context: RPCContext): Any? {
        return context.session
    }
}

