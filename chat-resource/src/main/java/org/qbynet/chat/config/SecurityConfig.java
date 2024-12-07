package org.qbynet.chat.config;

import jakarta.annotation.Resource;
import org.qbynet.chat.filter.BotAuthenticationFilter;
import org.qbynet.chat.filter.UserFilter;
import org.qbynet.chat.service.UserService;
import org.qbynet.chat.util.BotConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
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
        return http
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/media/*/download", "/api/media/*/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/user/profile").hasAuthority("SCOPE_profile.read")
                        .requestMatchers(HttpMethod.POST, "/api/user/profile").hasAuthority("SCOPE_profile.edit")
                        .requestMatchers("/api/link/**").hasAuthority("SCOPE_link")
                        .requestMatchers("/api/bot/create").hasAuthority("SCOPE_bot.create")
                        .requestMatchers("/api/message/send").hasAuthority("SCOPE_message.send")
                        .requestMatchers("/api/media/upload").hasAuthority("SCOPE_media.upload")
                        .requestMatchers("/api/conversation/").hasAuthority("SCOPE_conversation.create")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(conf -> conf
                        .jwt(Customizer.withDefaults())
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
