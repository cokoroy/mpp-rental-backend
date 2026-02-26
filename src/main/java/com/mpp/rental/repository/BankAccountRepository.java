package com.mpp.rental.repository;

import com.mpp.rental.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * BankAccountRepository - Data Access Layer for BankAccount entity
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    /**
     * Find bank account by user ID
     */
    Optional<BankAccount> findByUser_UserId(Long userId);

    /**
     * Check if bank account number already exists
     * (to prevent duplicate bank accounts)
     */
    boolean existsByBankAccNumber(String bankAccNumber);
}