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

import com.google.protobuf.GeneratedMessage
import org.cubewhy.qbychat.entity.User
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
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
class PayloadArgumentResolver : RPCArgumentResolver {

    object ProtobufParserCache {
        private val methodCache = ConcurrentHashMap<Class<*>, Method>()

        fun parseFrom(clazz: Class<*>, data: ByteArray): Any {
            val method = methodCache.computeIfAbsent(clazz) {
                it.getMethod("parseFrom", ByteArray::class.java)
            }
            return method.invoke(null, data)
        }
    }


    override fun supportsParameter(param: java.lang.reflect.Parameter): Boolean {
        return GeneratedMessage::class.java.isAssignableFrom(param.type)
    }

    override fun supportsKParameter(param: KParameter): Boolean {
        val clazz = (param.type.classifier as? KClass<*>)?.java ?: return false
        return GeneratedMessage::class.java.isAssignableFrom(clazz)
    }

    override fun resolveArgument(param: java.lang.reflect.Parameter, context: RPCContext): Any? {
        val payload = context.payload ?: throw IllegalStateException("Payload is null")
        return ProtobufParserCache.parseFrom(param.type, payload)
    }

    override fun resolveKArgument(param: KParameter, context: RPCContext): Any? {
        val clazz = (param.type.classifier as? KClass<*>)?.java
            ?: throw IllegalStateException("Could not determine parameter class")
        val payload = context.payload ?: throw IllegalStateException("Payload is null")
        return ProtobufParserCache.parseFrom(clazz, payload)
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

