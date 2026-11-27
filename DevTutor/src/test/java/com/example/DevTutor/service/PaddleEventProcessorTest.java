package com.example.DevTutor.service;

import com.example.DevTutor.entity.PaddleWebhookEvent;
import com.example.DevTutor.enums.WebhookEventStatus;
import com.example.DevTutor.repository.PaddleWebhookEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaddleEventProcessorTest {

    private static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");

    @Mock
    private PaddleWebhookEventRepository eventRepository;

    @Mock
    private PaddleSubscriptionHandler handler;

    private PaddleEventProcessor processor;
    private PaddleWebhookEvent event;

    @BeforeEach
    void setUp() {
        processor = new PaddleEventProcessor(eventRepository, handler);
        event = new PaddleWebhookEvent();
        event.setEventId("evt_1");
    }

    @Test
    void successMarksProcessed() {
        when(handler.handle(event)).thenReturn(WebhookEventStatus.PROCESSED);

        processor.process(event, NOW);

        assertEquals(WebhookEventStatus.PROCESSED, event.getStatus());
        verify(eventRepository).save(event);
    }

    @Test
    void failureIsRetriedInOneMinute() {
        when(handler.handle(event)).thenThrow(new IllegalStateException("No user"));

        processor.process(event, NOW);

        assertEquals(WebhookEventStatus.FAILED, event.getStatus());
        assertEquals(1, event.getAttempts());
        assertEquals(NOW.plus(Duration.ofMinutes(1)), event.getNextAttemptAt());
        assertNotNull(event.getLastError());
        verify(eventRepository).save(event);
    }

    @Test
    void tenthFailureMarksDead() {
        event.setAttempts(PaddleEventProcessor.MAX_ATTEMPTS - 1);
        when(handler.handle(event)).thenThrow(new IllegalStateException("No user"));

        processor.process(event, NOW);

        assertEquals(WebhookEventStatus.DEAD, event.getStatus());
        assertEquals(PaddleEventProcessor.MAX_ATTEMPTS, event.getAttempts());
        assertNull(event.getNextAttemptAt());
    }
}
