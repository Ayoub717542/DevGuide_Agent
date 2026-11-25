package com.example.DevTutor.repository;

import com.example.DevTutor.entity.PaddleWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaddleWebhookEventRepository extends JpaRepository<PaddleWebhookEvent, String> {
}
