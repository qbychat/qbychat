package org.cubewhy.qbychat.controller

import org.cubewhy.qbychat.entity.response.ClientDiscoveryResponse
import org.cubewhy.qbychat.service.WellKnownService
import org.cubewhy.qbychat.util.extractBaseUri
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/.well-known/qbychat")
class WellKnownController(private val wellKnownService: WellKnownService) {
    @GetMapping("client.json")
    suspend fun client(exchange: ServerWebExchange): ResponseEntity<ClientDiscoveryResponse> {
        return ResponseEntity.ok(wellKnownService.client(exchange.extractBaseUri()))
    }
}