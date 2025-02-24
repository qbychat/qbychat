package org.cubewhy.qbychat.util.security

import org.cubewhy.qbychat.service.PacketProcessor

class WebsocketSecurityRules(val rules: Map<String, Rule>) {
    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    class Builder {
        private val rules = mutableMapOf<String, Rule>()

        fun permitAll(service: Class<out PacketProcessor>, method: String): Builder {
            this.rules["${service.name}:$method"] = Rule.PERMIT_ALL
            return this
        }

        fun build(): WebsocketSecurityRules {
            return WebsocketSecurityRules(rules)
        }
    }

    enum class Rule {
        PERMIT_ALL
    }
}