package com.mpp.rental.repository;

import com.mpp.rental.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /**
     * Find payment by application ID
     */
    Optional<Payment> findByApplication_ApplicationId(Integer applicationId);
}