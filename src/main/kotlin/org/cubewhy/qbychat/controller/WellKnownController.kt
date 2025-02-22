package org.cubewhy.qbychat.controller

import org.cubewhy.qbychat.entity.vo.QbyChatConfigVO
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/.well-known")
class WellKnownController {
    @Value("\${qbychat.websocket.path}")
    private lateinit var websocketPath: String

    @GetMapping("qbychat-config")
    suspend fun qbyChatConfig(): QbyChatConfigVO {
        return QbyChatConfigVO(
            websocketPath = websocketPath
        )
    }
}