package org.qbynet.authorization.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.jackson2.CoreJackson2Module;

@Configuration
public class JsonConfig {
    @Bean
    @Primary
    ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new CoreJackson2Module());
    }
}
