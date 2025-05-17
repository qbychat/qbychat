package org.cubewhy.qbychat.service

import org.cubewhy.qbychat.entity.response.ClientDiscoveryResponse

interface WellKnownService {
    suspend fun client(baseUri: String): ClientDiscoveryResponse
}