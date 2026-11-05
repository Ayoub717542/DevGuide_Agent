package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.dto.quiz.QuizResponse;
import com.example.DevTutor.dto.quiz.QuizResponse.Question;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizServiceImplTest {

    private static final Question GOOD = new Question("What is 2+2?", List.of("3", "4", "5", "6"), 1, "Basic math.");

    private static QuizResponse quiz(Question last) {
        return new QuizResponse("math", List.of(GOOD, GOOD, last));
    }

    @Test
    void goodQuizIsValid() {
        assertTrue(QuizServiceImpl.isValid(quiz(GOOD)));
    }

    @Test
    void onlyThreeOptionsIsRejected() {
        assertFalse(QuizServiceImpl.isValid(quiz(new Question("What is 2+2?", List.of("3", "4", "5"), 1, "x"))));
    }

    @Test
    void correctIndexOutOfRangeIsRejected() {
        assertFalse(QuizServiceImpl.isValid(quiz(new Question("What is 2+2?", List.of("3", "4", "5", "6"), 7, "x"))));
    }

    @Test
    void missingQuizIsRejected() {
        assertFalse(QuizServiceImpl.isValid(null));
    }
}
