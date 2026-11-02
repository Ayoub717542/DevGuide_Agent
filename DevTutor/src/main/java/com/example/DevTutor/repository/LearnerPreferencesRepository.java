package com.example.DevTutor.repository;

import com.example.DevTutor.entity.LearnerPreferences;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface LearnerPreferencesRepository
        extends JpaRepository<LearnerPreferences, Long> {

    Optional<LearnerPreferences> findByUserEmail(String userEmail);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<LearnerPreferences> findLockedByUserEmail(String userEmail);
}
