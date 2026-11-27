package com.example.DevTutor.repository;

import com.example.DevTutor.entity.PaddleWebhookEvent;
import com.example.DevTutor.enums.WebhookEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface PaddleWebhookEventRepository extends JpaRepository<PaddleWebhookEvent, String> {

    @Query("""
            SELECT e FROM PaddleWebhookEvent e
            WHERE e.status IN :statuses
              AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now)
            ORDER BY e.occurredAt ASC
            """)
    List<PaddleWebhookEvent> findDue(Collection<WebhookEventStatus> statuses, Instant now, Pageable pageable);

    List<PaddleWebhookEvent> findByStatusOrderByOccurredAtAsc(WebhookEventStatus status);
}
