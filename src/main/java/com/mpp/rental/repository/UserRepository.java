package com.mpp.rental.repository;

import com.mpp.rental.model.User;
import com.mpp.rental.model.User.UserStatus;
import com.mpp.rental.model.User.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository - Data Access Layer for User entity
 * JpaRepository provides CRUD operations automatically
 * We add custom query methods here
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (for login)
     * Spring Data JPA automatically implements this based on method name
     */
    Optional<User> findByUserEmail(String userEmail);

    /**
     * Check if email already exists (for registration validation)
     */
    boolean existsByUserEmail(String userEmail);

    /**
     * Find users by status (for MPP to view pending approvals)
     */
    List<User> findByUserStatus(UserStatus userStatus);

    /**
     * Find users by category (for filtering)
     */
    List<User> findByUserCategory(UserCategory userCategory);

    /**
     * Find user by verification token (for email verification)
     */
    Optional<User> findByVerificationToken(String token);

    /**
     * Find all users with their businesses (using JOIN FETCH to avoid N+1 problem)
     * This is more efficient than lazy loading
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.businesses LEFT JOIN FETCH u.bankAccount")
    List<User> findAllWithBusinessesAndBankAccount();

    /**
     * Search users by multiple criteria
     * Searches in: name, email, phone number
     * Filters by: category, status
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.businesses " +
            "LEFT JOIN FETCH u.bankAccount " +
            "WHERE (:searchQuery IS NULL OR :searchQuery = '' OR " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(u.userEmail) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "u.userPhoneNumber LIKE CONCAT('%', :searchQuery, '%')) " +
            "AND (:category IS NULL OR u.userCategory = :category) " +
            "AND (:status IS NULL OR u.userStatus = :status)")
    List<User> searchUsers(
            @Param("searchQuery") String searchQuery,
            @Param("category") UserCategory category,
            @Param("status") UserStatus status
    );

    /**
     * Find user by ID with businesses and bank account (for details view)
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.businesses " +
            "LEFT JOIN FETCH u.bankAccount " +
            "WHERE u.userId = :userId")
    Optional<User> findByIdWithBusinessesAndBankAccount(@Param("userId") Long userId);
}