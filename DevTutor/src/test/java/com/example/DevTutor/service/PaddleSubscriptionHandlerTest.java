package com.example.DevTutor.service;

import com.example.DevTutor.entity.PaddleCheckout;
import com.example.DevTutor.entity.PaddleWebhookEvent;
import com.example.DevTutor.entity.User;
import com.example.DevTutor.enums.SubscriptionStatus;
import com.example.DevTutor.enums.WebhookEventStatus;
import com.example.DevTutor.repository.PaddleCheckoutRepository;
import com.example.DevTutor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaddleSubscriptionHandlerTest {

    private static final Instant T1 = Instant.parse("2026-11-26T10:00:00Z");
    private static final Instant T2 = Instant.parse("2026-11-26T11:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaddleCheckoutRepository checkoutRepository;

    private PaddleSubscriptionHandler handler;
    private User user;

    @BeforeEach
    void setUp() {
        handler = new PaddleSubscriptionHandler(userRepository, checkoutRepository);
        user = new User();
    }

    private PaddleWebhookEvent event(String type, Instant occurredAt, String data) {
        PaddleWebhookEvent event = new PaddleWebhookEvent();
        event.setEventId("evt_1");
        event.setEventType(type);
        event.setOccurredAt(occurredAt);
        event.setPayload("{\"data\":" + data + "}");
        return event;
    }

    @Test
    void createdGivesPro() {
        PaddleCheckout checkout = new PaddleCheckout();
        checkout.setUser(user);
        when(checkoutRepository.findById("txn_1")).thenReturn(Optional.of(checkout));

        WebhookEventStatus result = handler.handle(event("subscription.created", T1,
                "{\"id\":\"sub_1\",\"transaction_id\":\"txn_1\",\"status\":\"active\",\"scheduled_change\":null}"));

        assertEquals(WebhookEventStatus.PROCESSED, result);
        assertEquals(SubscriptionStatus.ACTIVE, user.getSubscriptionStatus());
        assertEquals("sub_1", user.getPaddleSubscriptionId());
        assertTrue(user.hasProAccess());
    }

    @Test
    void canceledRemovesPro() {
        user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        when(userRepository.findByPaddleSubscriptionId("sub_1")).thenReturn(Optional.of(user));

        handler.handle(event("subscription.canceled", T1,
                "{\"id\":\"sub_1\",\"status\":\"canceled\",\"scheduled_change\":null}"));

        assertEquals(SubscriptionStatus.CANCELED, user.getSubscriptionStatus());
        assertFalse(user.hasProAccess());
    }

    @Test
    void olderEventIsSkipped() {
        user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        user.setSubscriptionEventAt(T2);
        when(userRepository.findByPaddleSubscriptionId("sub_1")).thenReturn(Optional.of(user));

        WebhookEventStatus result = handler.handle(event("subscription.canceled", T1,
                "{\"id\":\"sub_1\",\"status\":\"canceled\",\"scheduled_change\":null}"));

        assertEquals(WebhookEventStatus.IGNORED, result);
        assertEquals(SubscriptionStatus.ACTIVE, user.getSubscriptionStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void scheduledCancelSetsDateAndRemovingItClearsIt() {
        when(userRepository.findByPaddleSubscriptionId("sub_1")).thenReturn(Optional.of(user));

        handler.handle(event("subscription.updated", T1,
                "{\"id\":\"sub_1\",\"status\":\"active\",\"scheduled_change\":"
                        + "{\"action\":\"cancel\",\"effective_at\":\"2026-12-26T10:00:00Z\"}}"));
        assertEquals(Instant.parse("2026-12-26T10:00:00Z"), user.getCancelEffectiveAt());

        handler.handle(event("subscription.updated", T2,
                "{\"id\":\"sub_1\",\"status\":\"active\",\"scheduled_change\":null}"));
        assertNull(user.getCancelEffectiveAt());
    }

    @Test
    void otherEventTypesAreIgnored() {
        WebhookEventStatus result = handler.handle(event("transaction.completed", T1, "{}"));

        assertEquals(WebhookEventStatus.IGNORED, result);
        verify(userRepository, never()).save(any());
    }
}
