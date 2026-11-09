package com.example.DevTutor.repository;

import com.example.DevTutor.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeRepository
        extends JpaRepository<Challenge, Long> {

    Optional<Challenge> findByIdAndUserEmail(Long id, String userEmail);

    void deleteByUserEmail(String userEmail);
}
