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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.qbychat.domain.model.Role
import org.cubewhy.qbychat.exception.WebsocketForbidden
import org.cubewhy.qbychat.exception.WebsocketNotFound
import org.cubewhy.qbychat.exception.WebsocketUnauthorized
import org.cubewhy.qbychat.websocket.protocol.v1.RPCRequestMethod
import org.springframework.aot.hint.ExecutableMode
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor
import org.springframework.beans.factory.support.RegisteredBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

data class RPCMethodDefinition(
    val bean: Any,
    val annotation: RPCMapping,
    val method: Method?,
    val kFunction: KFunction<*>?
)

class RPCHandlerRegistryRuntimeHints : RuntimeHintsRegistrar {

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        hints.reflection().registerType(RPCMapping::class.java, MemberCategory.DECLARED_FIELDS)

        hints.reflection().registerType(RPCContext::class.java, *MemberCategory.entries.toTypedArray())
        hints.reflection().registerType(RPCRequestMethod::class.java, *MemberCategory.entries.toTypedArray())
        hints.reflection().registerType(RPCPermissionFlag::class.java, *MemberCategory.entries.toTypedArray())
        hints.reflection().registerType(Role::class.java, *MemberCategory.entries.toTypedArray())

        hints.reflection().registerType(WebsocketNotFound::class.java, *MemberCategory.entries.toTypedArray())
        hints.reflection().registerType(WebsocketUnauthorized::class.java, *MemberCategory.entries.toTypedArray())
        hints.reflection().registerType(WebsocketForbidden::class.java, *MemberCategory.entries.toTypedArray())

        hints.reflection().registerType(KFunction::class.java, *MemberCategory.entries.toTypedArray())
        hints.reflection().registerType(KParameter::class.java, *MemberCategory.entries.toTypedArray())
        hints.reflection()
            .registerType(KParameter.Kind::class.java, *MemberCategory.entries.toTypedArray())
        hints.reflection()
            .registerType(kotlin.jvm.internal.CallableReference::class.java, *MemberCategory.entries.toTypedArray())

    }
}

@Component
@ImportRuntimeHints(RPCHandlerRegistryRuntimeHints::class)
class RPCHandlerRegistry : ApplicationContextAware, BeanRegistrationAotProcessor,
    ApplicationListener<ContextRefreshedEvent> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private lateinit var argumentResolvers: List<RPCArgumentResolver>
    private lateinit var applicationContext: ApplicationContext
    private val handlers: MutableMap<RPCRequestMethod, RPCMethodDefinition> = mutableMapOf()

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        argumentResolvers = applicationContext.getBeansOfType(RPCArgumentResolver::class.java).values.toList()
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        scanAndRegisterHandlers()
    }

    override fun processAheadOfTime(registeredBean: RegisteredBean): BeanRegistrationAotContribution? {
        val beanClass = registeredBean.beanClass

        if (AnnotationUtils.findAnnotation(beanClass, Component::class.java) != null) {
            // For Component-annotated beans, we need to analyze them for RPC methods
            // and ensure they are properly registered for reflection in native images
            return BeanRegistrationAotContribution { generationContext, beanRegistrationCode ->
                val reflectionHints = generationContext.runtimeHints.reflection()

                // Register the bean class itself
                reflectionHints.registerType(beanClass,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.DECLARED_FIELDS)

                // Process both Java and Kotlin methods
                val isKotlin = try {
                    beanClass.declaredAnnotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
                } catch (_: Exception) {
                    false
                }

                if (isKotlin) {
                    // For Kotlin classes, we need to register KFunction and parameters
                    try {
                        val kClass = beanClass.kotlin
                        kClass.declaredFunctions.forEach { function ->
                            function.annotations.find { it is RPCMapping }?.let { _ ->
                                // Register the function for reflection
                                reflectionHints.registerType(function.javaClass, *MemberCategory.entries.toTypedArray())

                                // Register parameter types for reflection
                                function.parameters.forEach { param ->
                                    reflectionHints.registerType(param.type.jvmErasure.java,
                                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                                        MemberCategory.INVOKE_PUBLIC_METHODS)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Log exception but continue processing - reflective access might not be available at build time
                        // This is expected in some cases during AOT processing
                        logger.error(e) { "RPC handler registry handler registration." }
                    }
                } else {
                    // For Java classes, we process methods directly
                    ReflectionUtils.doWithMethods(beanClass) { method ->
                        val annotation = AnnotationUtils.findAnnotation(method, RPCMapping::class.java)
                        if (annotation != null) {
                            // Register the method for reflection
                            reflectionHints.registerMethod(method, ExecutableMode.INVOKE)

                            // Register parameter types for reflection
                            method.parameterTypes.forEach { paramType ->
                                reflectionHints.registerType(paramType,
                                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                                    MemberCategory.INVOKE_PUBLIC_METHODS)
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    /**
     * Scan and register all method that has @RPCMapping annotation
     */
    private fun scanAndRegisterHandlers() {
        val beans = applicationContext.getBeansWithAnnotation(Component::class.java)

        for ((_, bean) in beans) {
            val kClass = bean::class
            val jClass = kClass.java
            val isKotlin = isKotlinClass(kClass)

            if (isKotlin) {
                // process Kotlin class
                kClass.declaredFunctions.forEach { function ->
                    function.findAnnotation<RPCMapping>()?.let { annotation ->
                        registerHandler(bean, annotation, function, null)
                    }
                }
            } else {
                // process Java class
                ReflectionUtils.doWithMethods(jClass) { method ->
                    val annotation = AnnotationUtils.findAnnotation(method, RPCMapping::class.java)
                    if (annotation != null) {
                        registerHandler(bean, annotation, null, method)
                    }
                }
            }
        }
    }

    /**
     * Register a RPC handler
     */
    private fun registerHandler(bean: Any, annotation: RPCMapping, kFunction: KFunction<*>?, method: Method?) {
        if (method == null) return
        val rpcMethod = annotation.method

        if (handlers.containsKey(rpcMethod)) {
            throw IllegalStateException("Duplicate RPC handler for method $rpcMethod: already registered by ${handlers[rpcMethod]?.method?.declaringClass?.name}")
        }

        handlers[rpcMethod] = RPCMethodDefinition(
            bean = bean,
            annotation = annotation,
            kFunction = kFunction,
            method = method
        )

        val handlerName = kFunction?.name ?: method.name
        val className = bean.javaClass.name
        val handlerType = if (kFunction != null) "Kotlin" else "Java"

        logger.debug { "Registered RPC handler ($handlerType): $rpcMethod -> $className.$handlerName" }
    }

    /**
     * Check a class is Kotlin class
     */
    private fun isKotlinClass(kClass: kotlin.reflect.KClass<*>): Boolean {
        return kClass.java.declaredAnnotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
    }

    /**
     * Invoke RPC handler
     */
    suspend fun invokeHandler(method: RPCRequestMethod, context: RPCContext): Any? {
        val def = handlers[method] ?: throw WebsocketNotFound("No RPC handler for method: $method")

        // Check permission
        checkPermissions(def, context)

        return when {
            def.kFunction != null -> invokeKotlinFunction(def, context)
            def.method != null -> invokeJavaMethod(def, context)
            else -> throw IllegalStateException("Bad RPC handler definition: cannot find a method to invoke.")
        }
    }

    /**
     * Invoke Java method
     */
    private fun invokeJavaMethod(def: RPCMethodDefinition, context: RPCContext): Any? {
        val method = def.method!!
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

        return ReflectionUtils.invokeMethod(method, def.bean, *resolvedArgs)
    }

    /**
     * Invoke Kotlin function
     */
    private suspend fun invokeKotlinFunction(def: RPCMethodDefinition, context: RPCContext): Any? {
        val kFunction = def.kFunction!!
        val parameters = kFunction.parameters
        val resolvedArgs = mutableListOf<Any?>()
        var rawArgIndex = 0

        for (param in parameters) {
            if (param.kind == KParameter.Kind.INSTANCE) {
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

    /**
     * Check permission
     */
    private fun checkPermissions(def: RPCMethodDefinition, context: RPCContext) {
        // Check if the permission allows anonymous (unregistered) clients
        if (RPCPermissionFlag.ALLOW_ANONYMOUS_ONLY == def.annotation.permissions) {
            // Ensure that the client is unregistered
            if (context.connection.metadata.clientId != null) {
                throw WebsocketUnauthorized("Client must not be registered.")
            }
        }

        // Check if the permission allows only unauthorized clients (registered but not logged in)
        if (RPCPermissionFlag.ALLOW_UNAUTHORIZED_ONLY == def.annotation.permissions) {
            // Ensure that the client is registered but the user is not logged in
            if (context.connection.metadata.clientId == null) {
                throw WebsocketUnauthorized("Client must be registered.")
            }
            if (context.user != null) {
                throw WebsocketUnauthorized("User must not be logged in.")
            }
        }

        // Check if the permission allows only authorized (logged in) users
        if (RPCPermissionFlag.ALLOW_AUTHORIZED_ONLY == def.annotation.permissions) {
            // Ensure that the user is logged in (authenticated)
            if (context.user == null) {
                throw WebsocketUnauthorized("User must be logged in.")
            }
        }

        if (RPCPermissionFlag.ALLOW_EXPECT_ANONYMOUS == def.annotation.permissions) {
            // Ensure that the client is registered
            if (context.connection.metadata.clientId == null) {
                throw WebsocketUnauthorized("Client must be registered.")
            }
        }

        // Check if the permission allows all users (no permission check)
        if (RPCPermissionFlag.ALLOW_ALL == def.annotation.permissions) {
            // No checks required, open access
            return
        }

        // Additional permission checks (if needed) can be added here
        // For example: Check if user roles are allowed for the method, etc.
        if (def.annotation.roles.isNotEmpty() && context.user != null && context.user.roles.none { it in def.annotation.roles }) {
            throw WebsocketForbidden("User does not have required roles.")
        }
    }
}