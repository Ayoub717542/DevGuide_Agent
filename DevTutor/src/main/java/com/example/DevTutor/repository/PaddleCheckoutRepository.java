package com.example.DevTutor.repository;

import com.example.DevTutor.entity.PaddleCheckout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaddleCheckoutRepository extends JpaRepository<PaddleCheckout, String> {
}
