package org.cubewhy.qbychat.util

import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.mono
import org.cubewhy.qbychat.entity.RestBean
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

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