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

package org.cubewhy.qbychat.shared.model

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
