package org.qbynet.chat.config;

import jakarta.annotation.Resource;
import org.qbynet.chat.filter.UserFilter;
import org.qbynet.chat.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
    @Resource
    UserService userService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf -> conf
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(conf -> conf
                        .jwt(Customizer.withDefaults())
                )
                .addFilterAfter(new UserFilter(userService), AuthorizationFilter.class)
                .build();
    }

    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }
}
