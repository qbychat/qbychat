package org.qbynet.chat.config;

import org.qbynet.chat.service.UserGraphqlService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQLServerConfiguration {
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer(UserGraphqlService userGraphqlService) {
        return builder -> {
            builder.type(
                "Query",
                wiring -> wiring
                    .dataFetcher("fetchUsers", environment -> userGraphqlService.getUsers())
            );
        };
    }
}
