package org.qbynet.chat.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OkHttpConfig {
    @Bean
    OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }
}
