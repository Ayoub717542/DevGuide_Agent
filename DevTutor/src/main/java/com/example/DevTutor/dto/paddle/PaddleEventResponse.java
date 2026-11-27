package com.example.DevTutor.dto.paddle;

import com.example.DevTutor.entity.PaddleWebhookEvent;
import com.example.DevTutor.enums.WebhookEventStatus;

import java.time.Instant;

public record PaddleEventResponse(
        String eventId,
        String eventType,
        Instant occurredAt,
        WebhookEventStatus status,
        int attempts,
        Instant nextAttemptAt,
        String lastError) {

    public static PaddleEventResponse from(PaddleWebhookEvent event) {
        return new PaddleEventResponse(
                event.getEventId(),
                event.getEventType(),
                event.getOccurredAt(),
                event.getStatus(),
                event.getAttempts(),
                event.getNextAttemptAt(),
                event.getLastError());
    }
}
