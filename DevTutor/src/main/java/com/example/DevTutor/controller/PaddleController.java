package com.example.DevTutor.controller;

import com.example.DevTutor.dto.paddle.PaddleEventResponse;
import com.example.DevTutor.enums.WebhookEventStatus;
import com.example.DevTutor.repository.PaddleWebhookEventRepository;
import com.example.DevTutor.service.PaddleCheckoutService;
import com.example.DevTutor.service.PaddleWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paddle")
@RequiredArgsConstructor
public class PaddleController {

    private final PaddleCheckoutService paddleCheckoutService;
    private final PaddleWebhookService paddleWebhookService;
    private final PaddleWebhookEventRepository eventRepository;

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Map<String, String> checkout(Authentication authentication) {
        return Map.of("transactionId", paddleCheckoutService.startCheckout(authentication.getName()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody byte[] body,
                                        @RequestHeader(value = "Paddle-Signature", required = false) String signature) {
        if (!paddleWebhookService.receive(body, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/events")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaddleEventResponse> events(@RequestParam WebhookEventStatus status) {
        return eventRepository.findByStatusOrderByOccurredAtAsc(status).stream()
                .map(PaddleEventResponse::from)
                .toList();
    }
}
