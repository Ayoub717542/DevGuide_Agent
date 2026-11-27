package com.example.DevTutor.service;

import com.example.DevTutor.entity.PaddleWebhookEvent;
import com.example.DevTutor.repository.PaddleWebhookEventRepository;
import com.example.DevTutor.security.PaddleSignatureVerifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;

@Service
public class PaddleWebhookService {

    private final PaddleSignatureVerifier verifier;
    private final PaddleWebhookEventRepository eventRepository;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public PaddleWebhookService(PaddleSignatureVerifier verifier,
                                PaddleWebhookEventRepository eventRepository) {
        this.verifier = verifier;
        this.eventRepository = eventRepository;
    }

    public boolean receive(byte[] rawBody, String signatureHeader) {
        if (!verifier.isValid(rawBody, signatureHeader)) {
            return false;
        }

        String payload = new String(rawBody, StandardCharsets.UTF_8);
        String eventId;
        String eventType;
        Instant occurredAt;
        try {
            JsonNode json = jsonMapper.readTree(payload);
            eventId = json.path("event_id").asString(null);
            eventType = json.path("event_type").asString(null);
            occurredAt = Instant.parse(json.path("occurred_at").asString(""));
        } catch (JacksonException | DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Paddle event");
        }
        if (eventId == null || eventType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Paddle event");
        }

        if (eventRepository.existsById(eventId)) {
            return true;
        }

        PaddleWebhookEvent event = new PaddleWebhookEvent();
        event.setEventId(eventId);
        event.setEventType(eventType);
        event.setOccurredAt(occurredAt);
        event.setPayload(payload);
        eventRepository.save(event);
        return true;
    }
}
