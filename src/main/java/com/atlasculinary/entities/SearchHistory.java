package com.atlasculinary.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID; // Cần import UUID nếu UserProfile.userId là UUID

@Entity
@Table(name = "search_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;


    @Column(name = "keyword", nullable = false, length = 500)
    private String keyword;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filters", columnDefinition = "json")
    private Map<String, Object> filters;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "session_identifier", length = 255)
    private String sessionIdentifier;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}