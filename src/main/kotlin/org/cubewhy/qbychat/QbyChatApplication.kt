package org.cubewhy.qbychat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class QbyChatApplication

fun main(args: Array<String>) {
    runApplication<QbyChatApplication>(*args)
}
