/*
 *  Copyright (c) 2025. All rights reserved.
 *  This file is a part of the QbyChat project
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.cubewhy.qbychat.interfaces.controller

import org.cubewhy.qbychat.application.service.WellKnownService
import org.cubewhy.qbychat.interfaces.controller.response.ClientDiscoveryResponse
import org.cubewhy.qbychat.shared.util.extractBaseUri
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