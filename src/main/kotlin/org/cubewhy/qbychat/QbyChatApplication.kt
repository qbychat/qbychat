package org.cubewhy.qbychat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing

@SpringBootApplication
@EnableReactiveMongoAuditing
class QbyChatApplication

fun main(args: Array<String>) {
    runApplication<QbyChatApplication>(*args)
}
