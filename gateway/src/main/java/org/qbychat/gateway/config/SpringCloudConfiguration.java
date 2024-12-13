package org.qbychat.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringCloudConfiguration {
    @Value("${qbychat.gateway.address.api}")
    String apiAddress;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("apiModule", r -> r.path("/" + apiAddress + "/**")
                        .uri("lb://api-service/api")
                )
                .build();
    }
}
