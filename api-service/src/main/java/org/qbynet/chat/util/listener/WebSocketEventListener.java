package org.qbynet.chat.util.listener;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.service.EventService;
import org.qbynet.chat.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Log4j2
@Component
public class WebSocketEventListener {
    @Resource
    EventService eventService;

    @Resource
    UserService userService;

    @EventListener
    public void handleWebSocketConnect(@NotNull SessionConnectedEvent event) {
        eventService.updateConversationStatus(userService.find(event));
    }

    @EventListener
    public void handleWebSocketDisconnect(@NotNull SessionDisconnectEvent event) {
        eventService.updateConversationStatus(userService.find(event));
    }
}
