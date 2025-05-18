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

package org.cubewhy.qbychat.shared.util

import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.shared.model.RestBean
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono


//fun ServerWebExchange.getNettyChannel(): Channel {
//    return (this.request as Connection).channel()
//}

fun ServerWebExchange.extractBaseUri(): String {
    val request = this.request
    val scheme = request.uri.scheme  // http or https
    val host = request.uri.host ?: throw IllegalStateException("Missing Host header")
    val port = request.uri.port.takeIf { it != -1 } ?: if (scheme == "http") 80 else 443

    return "$scheme://${host}:${port}"
}


fun <T> ServerWebExchange.responseSuccess(data: T?): Mono<Void> {
    this.response.statusCode = HttpStatus.OK
    this.response.headers.contentType = MediaType.APPLICATION_JSON
    return this.response.writeWith(
        this.response.bufferFactory()
            .wrap(RestBean.success<T?>(data).toJson().encodeToByteArray()).toMono()
    ).then(Mono.defer { this.response.setComplete() })
}

fun ServerWebExchange.responseFailure(code: Int, message: String): Mono<Void> {
    this.response.statusCode = HttpStatus.valueOf(code)
    this.response.headers.contentType = MediaType.APPLICATION_JSON
    return this.response.writeWith(
        this.response.bufferFactory()
            .wrap(RestBean.failure<Nothing?>(code, message).toJson().encodeToByteArray()).toMono()
    ).then(Mono.defer { this.response.setComplete() })
}

fun ServerWebExchange.streamData(publisher: Flux<DataBuffer>): Mono<Void> {
    this.response.headers.contentType = MediaType.APPLICATION_OCTET_STREAM
    return this.response.writeWith(publisher).then(mono {
        // release data buffer
        publisher.collect { DataBufferUtils.release(it) }
    }).then(Mono.defer { this.response.setComplete() })
}