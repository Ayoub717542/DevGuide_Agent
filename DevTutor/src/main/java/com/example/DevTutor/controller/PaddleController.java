package com.example.DevTutor.controller;

import com.example.DevTutor.service.PaddleCheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/paddle")
@RequiredArgsConstructor
public class PaddleController {

    private final PaddleCheckoutService paddleCheckoutService;

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Map<String, String> checkout(Authentication authentication) {
        return Map.of("transactionId", paddleCheckoutService.startCheckout(authentication.getName()));
    }
}
