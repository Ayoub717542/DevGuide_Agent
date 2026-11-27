package com.example.DevTutor.service;

import com.example.DevTutor.entity.PaddleWebhookEvent;
import com.example.DevTutor.enums.WebhookEventStatus;
import com.example.DevTutor.repository.PaddleWebhookEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class PaddleEventProcessor {

    static final int MAX_ATTEMPTS = 10;
    private static final Duration FIRST_RETRY = Duration.ofMinutes(1);
    private static final Duration MAX_RETRY = Duration.ofHours(6);
    private static final int BATCH_SIZE = 20;

    private final PaddleWebhookEventRepository eventRepository;
    private final PaddleSubscriptionHandler handler;

    public PaddleEventProcessor(PaddleWebhookEventRepository eventRepository,
                                PaddleSubscriptionHandler handler) {
        this.eventRepository = eventRepository;
        this.handler = handler;
    }

    @Scheduled(fixedDelay = 10_000)
    public void processDueEvents() {
        Instant now = Instant.now();
        List<PaddleWebhookEvent> events = eventRepository.findDue(
                List.of(WebhookEventStatus.RECEIVED, WebhookEventStatus.FAILED),
                now, PageRequest.of(0, BATCH_SIZE));
        for (PaddleWebhookEvent event : events) {
            process(event, now);
        }
    }

    void process(PaddleWebhookEvent event, Instant now) {
        try {
            event.setStatus(handler.handle(event));
            event.setNextAttemptAt(null);
            event.setLastError(null);
        } catch (Exception e) {
            int attempts = event.getAttempts() + 1;
            event.setAttempts(attempts);
            event.setLastError(shorten(e.toString()));
            if (attempts >= MAX_ATTEMPTS) {
                event.setStatus(WebhookEventStatus.DEAD);
                event.setNextAttemptAt(null);
                log.error("Paddle event {} is DEAD after {} attempts", event.getEventId(), attempts, e);
            } else {
                event.setStatus(WebhookEventStatus.FAILED);
                event.setNextAttemptAt(now.plus(retryDelay(attempts)));
                log.warn("Paddle event {} failed (attempt {}): {}", event.getEventId(), attempts, e.toString());
            }
        }
        eventRepository.save(event);
    }

    private Duration retryDelay(int attempts) {
        Duration delay = FIRST_RETRY.multipliedBy(1L << (attempts - 1));
        return delay.compareTo(MAX_RETRY) > 0 ? MAX_RETRY : delay;
    }

    private String shorten(String text) {
        return text.length() > 1000 ? text.substring(0, 1000) : text;
    }
}
