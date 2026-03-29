package com.spring.api.API.Controllers;

import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.spring.api.API.models.DTOs.Chat.Message;

@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate){
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message) {
        log.info("Received message: {}", message);
        return message;
    }

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload Message message, Principal principal){
        messagingTemplate.convertAndSendToUser(
            message.to(),          
            "/queue/messages",
            message                   
        );
    }
}
