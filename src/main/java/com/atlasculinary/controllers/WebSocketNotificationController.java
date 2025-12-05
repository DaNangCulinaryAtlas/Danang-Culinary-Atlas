package com.atlasculinary.controllers;

import com.atlasculinary.dtos.NotificationDto;
import com.atlasculinary.securities.CustomAccountDetails;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * WebSocket controller for handling real-time notifications
 */
@Controller
@AllArgsConstructor
public class WebSocketNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification to a specific user
     * @param email User's email (used as principal in WebSocket)
     * @param notification Notification DTO to send
     */
    public void sendNotificationToUser(String email, NotificationDto notification) {
        messagingTemplate.convertAndSendToUser(
                email,
                "/queue/notifications",
                notification
        );
    }

    /**
     * Broadcast notification to all connected users (for system-wide alerts)
     * @param notification Notification DTO to broadcast
     */
    public void broadcastNotification(NotificationDto notification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * Handle subscription from client
     * This is optional - can be used for logging or tracking connected users
     */
    @MessageMapping("/notifications/subscribe")
    @SendToUser("/queue/notifications")
    public String handleSubscription(@AuthenticationPrincipal CustomAccountDetails principal) {
        return "Subscribed to notifications for user: " + principal.getAccountId();
    }
}
