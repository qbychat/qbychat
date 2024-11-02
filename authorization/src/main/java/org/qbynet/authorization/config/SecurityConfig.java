package org.qbynet.authorization.config;

import org.qbynet.authorization.federation.FederatedIdentityAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf ->
                        conf
                                .requestMatchers("/user/login").permitAll()
                                .requestMatchers("/assets/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(conf ->
                        conf
                                .loginPage("/user/login")
                )
                .oauth2Login(conf ->
                        conf
                                .loginPage("/user/login")
                                .successHandler(authenticationSuccessHandler())
                )
                .build();
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new FederatedIdentityAuthenticationSuccessHandler();
    }
}
