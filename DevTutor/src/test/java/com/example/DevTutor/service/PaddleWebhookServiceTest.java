package com.example.DevTutor.service;

import com.example.DevTutor.entity.PaddleWebhookEvent;
import com.example.DevTutor.enums.WebhookEventStatus;
import com.example.DevTutor.repository.PaddleWebhookEventRepository;
import com.example.DevTutor.security.PaddleSignatureVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaddleWebhookServiceTest {

    private static final byte[] BODY = """
            {"event_id":"evt_1","event_type":"subscription.created","occurred_at":"2026-11-26T10:00:00.123456Z","data":{}}
            """.getBytes(StandardCharsets.UTF_8);

    @Mock
    private PaddleSignatureVerifier verifier;

    @Mock
    private PaddleWebhookEventRepository eventRepository;

    private PaddleWebhookService service;

    @BeforeEach
    void setUp() {
        service = new PaddleWebhookService(verifier, eventRepository);
    }

    @Test
    void badSignatureIsRejectedAndNothingSaved() {
        when(verifier.isValid(BODY, "bad")).thenReturn(false);

        assertFalse(service.receive(BODY, "bad"));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void newEventIsSaved() {
        when(verifier.isValid(BODY, "good")).thenReturn(true);
        when(eventRepository.existsById("evt_1")).thenReturn(false);

        assertTrue(service.receive(BODY, "good"));

        ArgumentCaptor<PaddleWebhookEvent> saved = ArgumentCaptor.forClass(PaddleWebhookEvent.class);
        verify(eventRepository).save(saved.capture());
        assertEquals("evt_1", saved.getValue().getEventId());
        assertEquals("subscription.created", saved.getValue().getEventType());
        assertEquals(Instant.parse("2026-11-26T10:00:00.123456Z"), saved.getValue().getOccurredAt());
        assertEquals(WebhookEventStatus.RECEIVED, saved.getValue().getStatus());
    }

    @Test
    void duplicateEventIsNotSavedAgain() {
        when(verifier.isValid(BODY, "good")).thenReturn(true);
        when(eventRepository.existsById("evt_1")).thenReturn(true);

        assertTrue(service.receive(BODY, "good"));
        verify(eventRepository, never()).save(any());
    }
}
