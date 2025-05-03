package org.cubewhy.qbychat.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.qbychat.avro.FederationMessage
import org.cubewhy.qbychat.service.SessionService
import org.cubewhy.qbychat.util.sendWithEncryption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer


@Configuration
class KafkaStreamConfig(
    private val scope: CoroutineScope,
) {

    @Bean
    fun qbychatWebsocketPayloadConsumer(sessionService: SessionService): Consumer<FederationMessage> {
        return Consumer { message ->
            scope.launch {
                sessionService.processWithSessionLocally(message.userId) { session ->
                    // push
                    session.sendWithEncryption(message.payload.array()).awaitFirstOrNull()
                }
            }
        }
    }
}