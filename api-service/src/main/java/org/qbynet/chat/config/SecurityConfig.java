package org.qbynet.chat.config;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.config.BotConfig;
import org.qbynet.chat.filter.BotAuthenticationFilter;
import org.qbynet.chat.filter.UserFilter;
import org.qbynet.chat.service.UserService;
import org.qbynet.chat.util.CustomAccessDeniedHandler;
import org.qbynet.chat.util.CustomAuthenticationEntryPoint;
import org.qbynet.chat.util.JwtRoleConverter;
import org.qbynet.chat.util.ReactiveUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
    @Lazy
    @Resource
    UserService userService;

    @Resource
    ReactiveUtil reactiveUtil;

    @Resource
    BotConfig botConfig;

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter());

        return http
            .authorizeExchange(conf -> conf
                .pathMatchers("/actuator", "/actuator/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(conf -> conf
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
                .accessDeniedHandler(new CustomAccessDeniedHandler(reactiveUtil))
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint(reactiveUtil))
            )
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .addFilterBefore(new BotAuthenticationFilter(botConfig, userService), SecurityWebFiltersOrder.HTTP_BASIC)
            .addFilterBefore(new UserFilter(userService, reactiveUtil), SecurityWebFiltersOrder.LAST)
            .build();
    }

    @Bean
    SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
