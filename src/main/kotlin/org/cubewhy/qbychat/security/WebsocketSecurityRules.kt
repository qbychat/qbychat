package org.cubewhy.qbychat.security

import org.cubewhy.qbychat.entity.Role
import org.cubewhy.qbychat.service.PacketProcessor

class WebsocketSecurityRules(
    val commonRules: Map<String, Rule>,
    val roleRules: Map<String, List<Role>>
) {
    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    class Builder {
        private val commonRules = mutableMapOf<String, Rule>()
        private val roleRules = mutableMapOf<String, List<Role>>()

        fun permitAll(service: Class<out PacketProcessor>, method: String): Builder {
            this.commonRules["${service.name}:$method"] = Rule.PERMIT_ALL
            return this
        }

        fun anyRoles(service: Class<out PacketProcessor>, method: String, vararg roles: Role): Builder {
            this.roleRules["${service.name}:$method"] = listOf(*roles)
            return this
        }

        fun build(): WebsocketSecurityRules {
            return WebsocketSecurityRules(commonRules, roleRules)
        }
    }

    enum class Rule {
        PERMIT_ALL
    }
}