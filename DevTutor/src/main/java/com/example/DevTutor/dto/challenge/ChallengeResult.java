package com.example.DevTutor.dto.challenge;

public record ChallengeResult(
        String status,
        int passed,
        int total,
        String output) {
}
