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

package org.cubewhy.qbychat

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.Network
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    companion object {
        private val network: Network? = Network.newNetwork()
    }

    @Bean
    @ServiceConnection
    fun mongoDbContainer(): MongoDBContainer {
        return MongoDBContainer(DockerImageName.parse("mongo"))
            .withNetwork(network)
            .withNetworkAliases("mongo")
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "test")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "test")
            .withEnv("MONGO_INITDB_DATABASE", "qbychat-test")
            .withCreateContainerCmdModifier {
                it.hostConfig?.withMemory(512 * 1024 * 1024L)
            }
            .withReuse(true)
    }

    @Bean
    fun valkeyContainer(): GenericContainer<*> {
        return GenericContainer(DockerImageName.parse("valkey/valkey:8.1.1"))
            .withNetwork(network)
            .withNetworkAliases("valkey")
            .withExposedPorts(6379)
            .withReuse(true)
    }

    @Bean
    @ServiceConnection
    fun kafkaContainer(): KafkaContainer {
        return KafkaContainer(DockerImageName.parse("apache/kafka:4.0.0"))
            .withNetwork(network)
            .withNetworkAliases("kafka")
            .withReuse(true)
    }

    @Bean
    fun schemaRegistryContainer(kafkaContainer: KafkaContainer): GenericContainer<*> {
        return GenericContainer(DockerImageName.parse("confluentinc/cp-schema-registry:latest"))
            .withNetwork(network)
            .withNetworkAliases("schema-registry")
            .withExposedPorts(8081)
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "kafka:29092")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
            .dependsOn(kafkaContainer)
            .withReuse(true)
    }
}