package org.qbynet.chat.config;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.config.BotConfig;
import org.qbynet.chat.filter.BotAuthenticationFilter;
import org.qbynet.chat.filter.UserFilter;
import org.qbynet.chat.service.UserService;
import org.qbynet.chat.util.CustomAccessDeniedHandler;
import org.qbynet.chat.util.CustomAuthenticationEntryPoint;
import org.qbynet.chat.util.JwtRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
    @Lazy
    @Resource
    UserService userService;

    @Resource
    BotConfig botConfig;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter());

        return http
            .authorizeHttpRequests(conf -> conf
                .requestMatchers("/actuator", "/actuator/**", "/graphql").permitAll()
                .requestMatchers("/api/media/*/raw", "/api/media/*/info", "/api/avatar/image", "/api/avatar/list").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(conf -> conf
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
                .accessDeniedHandler(new CustomAccessDeniedHandler())
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
            )
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterAfter(new BotAuthenticationFilter(botConfig, userService), BasicAuthenticationFilter.class)
            .addFilterAfter(new UserFilter(userService), AuthorizationFilter.class)
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
