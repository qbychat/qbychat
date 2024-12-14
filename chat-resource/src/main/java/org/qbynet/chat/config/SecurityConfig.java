package org.qbynet.chat.config;

import jakarta.annotation.Resource;
import org.qbynet.chat.filter.BotAuthenticationFilter;
import org.qbynet.chat.filter.UserFilter;
import org.qbynet.chat.service.UserService;
import org.qbynet.chat.util.BotConfig;
import org.qbynet.chat.util.CustomAccessDeniedHandler;
import org.qbynet.chat.util.CustomAuthenticationEntryPoint;
import org.qbynet.chat.util.JwtRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

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
                        .requestMatchers("/actuator", "/actuator/**").permitAll()
                        .requestMatchers("/api/media/*/raw", "/api/media/*/info", "/api/avatar/image", "/api/avatar/list").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/user/profile").hasAuthority("SCOPE_profile.read")
                        .requestMatchers(HttpMethod.POST, "/api/user/profile").hasAuthority("SCOPE_profile.edit")
                        .requestMatchers("/api/link/**").hasAuthority("SCOPE_link")
                        .requestMatchers("/api/search/**").hasAuthority("SCOPE_search")
                        .requestMatchers("/api/bot/create").hasAuthority("SCOPE_bot.create")
                        .requestMatchers("/api/bot/delete").hasAuthority("SCOPE_bot.delete")
                        .requestMatchers("/api/bot/list").hasAuthority("SCOPE_bot.list")
                        .requestMatchers("/api/message/send").hasAuthority("SCOPE_message.send")
                        .requestMatchers("/api/media/upload").hasAuthority("SCOPE_media.upload")
                        .requestMatchers("/api/conversation/create").hasAuthority("SCOPE_conversation.create")
                        .requestMatchers("/api/conversation/list").hasAuthority("SCOPE_conversation.list")
                        .requestMatchers("/api/conversation/*/join").hasAuthority("SCOPE_conversation.join")
                        .requestMatchers("/api/avatar/**").hasAuthority("SCOPE_avatar.manage")
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
