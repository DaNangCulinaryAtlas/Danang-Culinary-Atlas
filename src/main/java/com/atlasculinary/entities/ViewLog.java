package com.atlasculinary.entities;

import com.atlasculinary.enums.EventType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "views_log", indexes = {
        @Index(name = "idx_views_log_restaurant_time", columnList = "restaurant_id, timestamp")
})

@Data
public class ViewLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long viewLogId;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "dish_id", nullable = false)
    private UUID dishId;

    @Column(name = "event_type", length = 20, nullable = false)
    private EventType eventType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "user_id")
    private UUID userId;

    // IP Address hoặc Session ID cho Guest
    @Column(name = "session_identifier", length = 255)
    private String sessionIdentifier;
}