package com.atlasculinary.entities;

import com.atlasculinary.enums.EventType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "search_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long searchLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private SearchHistory searchHistory;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(name = "dish_id")
    private UUID dishId;

    @Column(name = "event_type", length = 20, nullable = false)
    private EventType eventType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}