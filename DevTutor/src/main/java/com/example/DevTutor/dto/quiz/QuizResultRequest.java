package com.example.DevTutor.dto.quiz;

public record QuizResultRequest(
        String topic,
        int score,
        int total)
{
}
