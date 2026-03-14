package com.bidpulse.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "notification", indexes = {
    @Index(name = "idx_notification_user_seen", columnList = "user_id, seen")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private NotificationType type;

    @Column(columnDefinition = "text")
    private String message;

    // JSON payload or additional data as string (Postgres can store as jsonb via migrations)
    @Column(columnDefinition = "text")
    private String data;

    @Column(nullable = false)
    private boolean seen = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}