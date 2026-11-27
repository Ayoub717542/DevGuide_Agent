package com.example.DevTutor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class PaddleClient {

    private final RestClient restClient;
    private final String priceId;

    public PaddleClient(@Value("${app.paddle.api-key}") String apiKey,
                        @Value("${app.paddle.base-url}") String baseUrl,
                        @Value("${app.paddle.price-id}") String priceId) {
        this.priceId = priceId;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Paddle-Version", "1")
                .build();
    }

    public String createTransaction() {
        Map<String, Object> body = Map.of(
                "items", List.of(Map.of("price_id", priceId, "quantity", 1)));

        Map<String, Object> response = restClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || !(response.get("data") instanceof Map<?, ?> data)
                || !(data.get("id") instanceof String transactionId)) {
            throw new IllegalStateException("Paddle response has no transaction id");
        }
        return transactionId;
    }

    public void cancelSubscription(String subscriptionId) {
        restClient.post()
                .uri("/subscriptions/{id}/cancel", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("effective_from", "next_billing_period"))
                .retrieve()
                .toBodilessEntity();
    }
}
