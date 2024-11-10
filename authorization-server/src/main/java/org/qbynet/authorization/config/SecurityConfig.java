package org.qbynet.authorization.config;

import org.qbynet.authorization.federation.FederatedIdentityAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/assets/**", "/favicon.ico", "/.well-known/**").permitAll()
                        .requestMatchers("/user/login", "/user/register", "/user/confirm").anonymous()
                        .anyRequest().authenticated()
                )
                .formLogin(conf -> conf
                        .loginPage("/user/login")
                )
                .logout(conf -> conf
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/user/login?logout")
                )
                .oauth2Login(conf -> conf
                        .loginPage("/user/login")
                        .successHandler(authenticationSuccessHandler())
                )
                .build();
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new FederatedIdentityAuthenticationSuccessHandler();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

}
