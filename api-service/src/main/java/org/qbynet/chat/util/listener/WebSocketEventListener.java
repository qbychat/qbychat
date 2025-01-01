package org.qbynet.chat.util.listener;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Log4j2
@Component
public class WebSocketEventListener {
    @Resource
    UserService userService;

    @EventListener
    public void handleWebSocketConnect(@NotNull SessionConnectedEvent event) {
        // todo push events for connect
    }

    @EventListener
    public void handleWebSocketDisconnect(@NotNull SessionDisconnectEvent event) {
        // todo push events for disconnect
    }
}
