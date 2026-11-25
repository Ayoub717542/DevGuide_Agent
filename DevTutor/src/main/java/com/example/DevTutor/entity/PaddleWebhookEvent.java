package com.example.DevTutor.entity;

import com.example.DevTutor.enums.WebhookEventStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "paddle_webhook_events")
@Getter
@Setter
@NoArgsConstructor
public class PaddleWebhookEvent {

    @Id
    @Column(length = 64)
    private String eventId;

    @Column(nullable = false, length = 64)
    private String eventType;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WebhookEventStatus status = WebhookEventStatus.RECEIVED;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int attempts;

    private Instant nextAttemptAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false, updatable = false)
    private Instant receivedAt;

    @PrePersist
    private void onCreate() {
        receivedAt = Instant.now();
    }
}
