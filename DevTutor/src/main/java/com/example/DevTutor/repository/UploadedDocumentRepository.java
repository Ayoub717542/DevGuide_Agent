package com.example.DevTutor.repository;

import com.example.DevTutor.entity.UploadedDocument;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, Long> {

    List<UploadedDocument> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    boolean existsByConversationId(Long conversationId);

    Optional<UploadedDocument> findByDocumentIdAndConversationCreatedByEmail(
            String documentId, String email);

    @EntityGraph(attributePaths = "conversation")
    List<UploadedDocument> findByConversationCreatedByEmailOrderByCreatedAtDesc(String email);

    void deleteByConversationId(Long conversationId);
}
