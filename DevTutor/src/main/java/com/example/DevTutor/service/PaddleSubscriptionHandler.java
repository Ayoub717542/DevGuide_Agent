package com.example.DevTutor.service;

import com.example.DevTutor.entity.PaddleCheckout;
import com.example.DevTutor.entity.PaddleWebhookEvent;
import com.example.DevTutor.entity.User;
import com.example.DevTutor.enums.SubscriptionStatus;
import com.example.DevTutor.enums.WebhookEventStatus;
import com.example.DevTutor.repository.PaddleCheckoutRepository;
import com.example.DevTutor.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Set;

@Service
public class PaddleSubscriptionHandler {

    private static final Set<String> SUBSCRIPTION_EVENTS = Set.of(
            "subscription.created", "subscription.updated", "subscription.canceled");

    private final UserRepository userRepository;
    private final PaddleCheckoutRepository checkoutRepository;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public PaddleSubscriptionHandler(UserRepository userRepository,
                                     PaddleCheckoutRepository checkoutRepository) {
        this.userRepository = userRepository;
        this.checkoutRepository = checkoutRepository;
    }

    @Transactional
    public WebhookEventStatus handle(PaddleWebhookEvent event) {
        if (!SUBSCRIPTION_EVENTS.contains(event.getEventType())) {
            return WebhookEventStatus.IGNORED;
        }

        JsonNode data = jsonMapper.readTree(event.getPayload()).path("data");
        String subscriptionId = data.path("id").asString(null);
        String transactionId = data.path("transaction_id").asString(null);
        String status = data.path("status").asString(null);
        if (subscriptionId == null || status == null) {
            throw new IllegalStateException("Subscription event without id or status");
        }

        User user = findUser(transactionId, subscriptionId);

        Instant lastEventAt = user.getSubscriptionEventAt();
        if (lastEventAt != null && event.getOccurredAt().isBefore(lastEventAt)) {
            return WebhookEventStatus.IGNORED;
        }

        user.setSubscriptionStatus(SubscriptionStatus.valueOf(status.toUpperCase()));
        user.setPaddleSubscriptionId(subscriptionId);
        user.setSubscriptionEventAt(event.getOccurredAt());
        user.setCancelEffectiveAt(cancelDate(data.path("scheduled_change")));
        userRepository.save(user);
        return WebhookEventStatus.PROCESSED;
    }

    private User findUser(String transactionId, String subscriptionId) {
        if (transactionId != null) {
            User user = checkoutRepository.findById(transactionId)
                    .map(PaddleCheckout::getUser)
                    .orElse(null);
            if (user != null) {
                return user;
            }
        }
        return userRepository.findByPaddleSubscriptionId(subscriptionId)
                .orElseThrow(() -> new IllegalStateException(
                        "No user for subscription " + subscriptionId));
    }

    private Instant cancelDate(JsonNode scheduledChange) {
        if (!"cancel".equals(scheduledChange.path("action").asString(null))) {
            return null;
        }
        return Instant.parse(scheduledChange.path("effective_at").asString());
    }
}
