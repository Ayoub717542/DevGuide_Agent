package com.example.DevTutor.dto.quiz;

import com.example.DevTutor.entity.QuizResult;

import java.time.LocalDateTime;

public record QuizResultResponse(
        String topic,
        int score,
        int total,
        LocalDateTime createdAt) {

    public static QuizResultResponse from(QuizResult result) {
        return new QuizResultResponse(
                result.getTopic(),
                result.getScore(),
                result.getTotal(),
                result.getCreatedAt());
    }
}
