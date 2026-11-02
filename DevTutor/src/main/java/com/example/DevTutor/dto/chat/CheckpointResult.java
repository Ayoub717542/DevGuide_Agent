package com.example.DevTutor.dto.chat;

public record CheckpointResult(
        String summary,
        String experienceLevel,
        String explanationStyle,
        String responseLength,
        String codePreference) {
}
