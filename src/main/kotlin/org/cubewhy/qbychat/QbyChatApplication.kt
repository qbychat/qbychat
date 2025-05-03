package org.cubewhy.qbychat

import org.cubewhy.qbychat.entity.config.InstanceProperties
import org.cubewhy.qbychat.entity.config.QbyChatProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing

@SpringBootApplication
@EnableReactiveMongoAuditing
@EnableConfigurationProperties(QbyChatProperties::class, InstanceProperties::class)
class QbyChatApplication

fun main(args: Array<String>) {
    runApplication<QbyChatApplication>(*args)
}
