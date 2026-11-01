package com.example.DevTutor.entity;

import com.example.DevTutor.enums.CodePreference;
import com.example.DevTutor.enums.ExperienceLevel;
import com.example.DevTutor.enums.ExplanationStyle;
import com.example.DevTutor.enums.ResponseLength;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LearnerPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    @Enumerated(EnumType.STRING)
    private ExplanationStyle explanationStyle;

    @Enumerated(EnumType.STRING)
    private ResponseLength responseLength;

    @Enumerated(EnumType.STRING)
    private CodePreference codePreference;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
