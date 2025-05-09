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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.qbychat.util.isKotlinClass
import org.cubewhy.qbychat.websocket.protocol.v1.RequestMethod
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

data class RPCMethodDefinition(
    val bean: Any,
    val annotation: RPCHandler,
    val method: Method?,
    val kFunction: KFunction<*>?
)

@Component
class RPCHandlerRegistry : ApplicationContextAware {

    companion object {
        private val logger = KotlinLogging.logger {}
    }


    private lateinit var argumentResolvers: List<RPCArgumentResolver>
    private lateinit var applicationContext: ApplicationContext
    private val handlers: MutableMap<RequestMethod, RPCMethodDefinition> = mutableMapOf()

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        argumentResolvers = applicationContext.getBeansOfType(RPCArgumentResolver::class.java).values.toList()
    }

    @EventListener(ApplicationReadyEvent::class)
    private fun registerHandlersOnReady() {
        val beans = applicationContext.getBeansWithAnnotation(Component::class.java)

        for ((_, bean) in beans) {
            val kClass = bean::class
            val jClass = kClass.java

            val functions = if (isKotlinClass(kClass)) {
                kClass.declaredFunctions.filter { it.findAnnotation<RPCHandler>() != null }.map { function ->
                    val annotation = function.findAnnotation<RPCHandler>()!!
                    Triple(annotation.method, annotation, function to null)
                }
            } else {
                jClass.declaredMethods.mapNotNull { method ->
                    val annotation =
                        AnnotationUtils.findAnnotation(method, RPCHandler::class.java) ?: return@mapNotNull null
                    Triple(annotation.method, annotation, null to method)
                }
            }

            for ((key, annotation, pair) in functions) {
                if (handlers.containsKey(key)) {
                    throw IllegalStateException("Duplicate RPC handler for method $key: already registered by ${handlers[key]?.method?.declaringClass?.name}")
                }

                val (kFunction, method) = pair
                handlers[key] = RPCMethodDefinition(
                    bean = bean,
                    annotation = annotation,
                    kFunction = kFunction,
                    method = method
                )

                logger.debug {
                    val name = kFunction?.name ?: method?.name
                    val type = if (kFunction != null) "Kotlin" else "Java"
                    "Registered RPC handler ($type): $key -> ${jClass.name}.$name"
                }
            }
        }
    }

    suspend fun invokeHandler(method: RequestMethod, context: RPCContext): Any? {
        val def = handlers[method] ?: throw IllegalArgumentException("No RPC handler for method: $method")
        def.method?.let { method ->
            val parameters = method.parameters
            val resolvedArgs = arrayOfNulls<Any?>(parameters.size)

            var rawArgIndex = 0

            for ((i, param) in parameters.withIndex()) {
                val resolver = argumentResolvers.find { it.supportsParameter(param) }
                if (resolver != null) {
                    resolvedArgs[i] = resolver.resolveArgument(param, context)
                    continue
                }

                val springBean = try {
                    applicationContext.getBean(param.type)
                } catch (_: Exception) {
                    null
                }

                if (springBean != null) {
                    resolvedArgs[i] = springBean
                    continue
                }

                if (rawArgIndex >= context.rawArgs.size) {
                    throw IllegalArgumentException("Missing argument for parameter ${param.name}")
                }

                resolvedArgs[i] = context.rawArgs[rawArgIndex++]
            }

            return method.invoke(def.bean, *resolvedArgs)
        }

        def.kFunction?.let { kFunction ->
            val parameters = kFunction.parameters
            val resolvedArgs = mutableListOf<Any?>()
            var rawArgIndex = 0

            for (param in parameters) {
                if (param.kind == kotlin.reflect.KParameter.Kind.INSTANCE) {
                    resolvedArgs += def.bean
                    continue
                }

                val resolver = argumentResolvers.find { it.supportsKParameter(param) }
                if (resolver != null) {
                    resolvedArgs += resolver.resolveKArgument(param, context)
                    continue
                }

                val springBean = try {
                    applicationContext.getBean(param.type.jvmErasure.java)
                } catch (_: Exception) {
                    null
                }

                if (springBean != null) {
                    resolvedArgs += springBean
                    continue
                }

                if (rawArgIndex >= context.rawArgs.size) {
                    throw IllegalArgumentException("Missing argument for parameter ${param.name}")
                }

                resolvedArgs += context.rawArgs[rawArgIndex++]
            }

            return if (kFunction.isSuspend) {
                kFunction.callSuspend(*resolvedArgs.toTypedArray())
            } else {
                kFunction.call(*resolvedArgs.toTypedArray())
            }
        }
        throw IllegalStateException("Bad RPC handler define: cannot find a method to invoke.")
    }


    fun getRegisteredHandlers(): Map<RequestMethod, RPCMethodDefinition> = handlers.toMap()
}