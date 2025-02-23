package org.cubewhy.qbychat.controller

import org.cubewhy.qbychat.entity.vo.QbyChatConnectionConfigVO
import org.cubewhy.qbychat.entity.vo.QbyChatRulesVO
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/.well-known/qbychat")
class WellKnownController {
    @Value("\${qbychat.websocket.path}")
    private lateinit var websocketPath: String

    @Value("\${qbychat.user.username.rule.regex}")
    private lateinit var usernameRuleRegex: String

    @Value("\${qbychat.user.username.rule.description}")
    private lateinit var usernameRuleDescription: String


    @GetMapping("conn-config")
    suspend fun qbyChatConnectionConfig(): QbyChatConnectionConfigVO {
        return QbyChatConnectionConfigVO(
            websocketPath = websocketPath
        )
    }

    @GetMapping("rules")
    suspend fun qbyChatRules(): QbyChatRulesVO {
        return QbyChatRulesVO(
            usernameRegex = usernameRuleRegex,
            usernameRule = usernameRuleDescription
        )
    }
}