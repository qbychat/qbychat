package org.qbynet.chat.util;

import org.jetbrains.annotations.NotNull;
import org.qbynet.shared.entity.RestBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

@Component
public class ReactiveUtil {
    public <T> Mono<Void> withRestBean(@NotNull ServerWebExchange exchange, @NotNull RestBean<T> bean) {
        return Mono.defer(() -> Mono.just(exchange.getResponse())).flatMap((response) -> {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            DataBuffer buffer = dataBufferFactory.wrap(bean.toJson().getBytes(Charset.defaultCharset()));
            return response.writeWith(Mono.just(buffer))
                .doOnError((error) -> DataBufferUtils.release(buffer));
        });
    }
}
