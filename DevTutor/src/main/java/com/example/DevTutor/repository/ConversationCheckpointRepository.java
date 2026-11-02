package com.example.DevTutor.repository;

import com.example.DevTutor.entity.ConversationCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ConversationCheckpointRepository
        extends JpaRepository<ConversationCheckpoint, Long> {

    Optional<ConversationCheckpoint> findByConversationId(Long conversationId);

    @Transactional
    long deleteByConversationId(Long conversationId);
}
