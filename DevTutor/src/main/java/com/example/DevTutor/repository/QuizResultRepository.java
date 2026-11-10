package com.example.DevTutor.repository;

import com.example.DevTutor.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository
        extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findTop5ByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<QuizResult> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    void deleteByUserEmail(String userEmail);
}
