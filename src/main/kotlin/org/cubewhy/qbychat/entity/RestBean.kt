package org.cubewhy.qbychat.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class RestBean<T>(
    @JsonProperty("code")
    val code: Int,
    @JsonProperty("data")
    val data: T?,
    @JsonProperty("message")
    val message: String
) {
    companion object {
        fun <T> success(data: T?): RestBean<T> {
            return RestBean(200, data, "Request successful")
        }

        fun <T> success(): RestBean<T> {
            return success(null)
        }

        fun <T> unauthorized(message: String): RestBean<T> {
            return failure(401, message)
        }

        fun <T> unauthorized(exception: RuntimeException): RestBean<T> {
            return unauthorized(exception.message ?: "Unauthorized")
        }

        fun <T> forbidden(message: String): RestBean<T> {
            return failure(403, message)
        }

        fun <T> forbidden(exception: RuntimeException): RestBean<T> {
            return forbidden(exception.message ?: "Forbidden")
        }

        fun <T> failure(code: Int, message: String): RestBean<T> {
            return RestBean(code, null, message)
        }

        fun <T> failure(code: Int, exception: RuntimeException): RestBean<T> {
            return failure(code, exception.message ?: "Error")
        }

        fun <T> badRequest(message: String): RestBean<T> {
            return failure(400, message)
        }

        fun <T> badRequest(): RestBean<T> {
            return badRequest("Bad request")
        }
    }

    fun toJson(): String {
        val mapper = jacksonObjectMapper()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        return mapper.writeValueAsString(this)
    }
}
