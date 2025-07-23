package com.javac2.framework.JavaC2Framework.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventWebSocketController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcast(String eventType, String agentId) {
        messagingTemplate.convertAndSend("/topic/events", new EventMessage(eventType, agentId));
    }

    public static class EventMessage {
        public String type;
        public String agentId;
        public EventMessage(String type, String agentId) {
            this.type = type;
            this.agentId = agentId;
        }
    }
} 