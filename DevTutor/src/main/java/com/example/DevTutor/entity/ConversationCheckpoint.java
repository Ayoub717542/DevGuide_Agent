package com.example.DevTutor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ConversationCheckpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long conversationId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    private long messageCount;

    @Column(columnDefinition = "TEXT")
    private String pendingPreferences;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
