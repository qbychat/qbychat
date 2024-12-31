package org.qbynet.chat.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(@NotNull StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket/sockjs").withSockJS();
        registry.addEndpoint("/websocket");
    }

    @Override
    public void configureMessageBroker(@NotNull MessageBrokerRegistry registry) {
//        registry.enableStompBrokerRelay("/topic", "/queue");
//        registry.setApplicationDestinationPrefixes("/app");
    }
}
