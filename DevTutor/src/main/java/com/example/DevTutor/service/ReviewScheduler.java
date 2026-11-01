package com.example.DevTutor.service;

import com.example.DevTutor.dto.quiz.ReviewResponse;
import com.example.DevTutor.entity.QuizResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReviewScheduler {

    public List<ReviewResponse> schedule(List<QuizResult> newestFirst) {
        Map<String, List<QuizResult>> byConcept = new LinkedHashMap<>();
        for (QuizResult result : newestFirst) {
            byConcept.computeIfAbsent(result.getTopic(), k -> new ArrayList<>()).add(result);
        }

        LocalDateTime now = LocalDateTime.now();
        List<ReviewResponse> reviews = new ArrayList<>();

        for (Map.Entry<String, List<QuizResult>> entry : byConcept.entrySet()) {
            List<QuizResult> results = entry.getValue();

            int streak = 0;
            while (streak < results.size()
                    && results.get(streak).getScore() == results.get(streak).getTotal()) {
                streak++;
            }

            long days = streak == 0 ? 1 : 1L << Math.min(streak, 6);
            QuizResult latest = results.get(0);
            LocalDateTime nextReview = latest.getCreatedAt().plusDays(days);

            reviews.add(new ReviewResponse(
                    entry.getKey(),
                    latest.getScore(),
                    latest.getTotal(),
                    nextReview,
                    !nextReview.isAfter(now)));
        }

        reviews.sort(Comparator.comparing(ReviewResponse::due).reversed()
                .thenComparing(ReviewResponse::nextReview));
        return reviews;
    }
}
