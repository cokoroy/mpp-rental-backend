package com.mpp.rental.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User Entity - Represents all users in the system
 * Includes: Business Owners (Student/Non-Student) and MPP administrators
 */
@Entity
@Table(name = "users") // 'user' is a reserved keyword in MySQL, so we use 'users'
@Data // Lombok: Auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", nullable = false, length = 255)
    private String userName;

    @Column(name = "user_email", nullable = false, unique = true, length = 255)
    private String userEmail;

    @Column(name = "user_phone_number", nullable = false, length = 20)
    private String userPhoneNumber;

    @Column(name = "user_password", nullable = false, length = 255)
    private String userPassword; // Will be encrypted with BCrypt

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Business> businesses;

    /**
     * User Category: MPP, STUDENT, NON_STUDENT
     * We'll use ENUM for better type safety
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_category", nullable = false, length = 50)
    private UserCategory userCategory;

    /**
     * User Status: PENDING, ACTIVE, BLOCKED
     * PENDING: New registrations waiting for MPP approval
     * ACTIVE: Approved and can use the system
     * BLOCKED: Blocked by MPP
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 50)
    private UserStatus userStatus = UserStatus.PENDING; // Default to PENDING

    @Column(name = "user_address", length = 500)
    private String userAddress;

    @CreationTimestamp // Automatically sets timestamp when record is created
    @Column(name = "user_registered_at", nullable = false, updatable = false)
    private LocalDateTime userRegisteredAt;

    @Column(name = "user_last_login")
    private LocalDateTime userLastLogin;

    /**
     * One-to-One relationship with BankAccount
     * mappedBy: tells JPA that BankAccount owns the relationship
     * cascade: operations on User will cascade to BankAccount
     * orphanRemoval: if BankAccount is removed from User, delete it
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private BankAccount bankAccount;

    /**
     * Email verification fields (for future implementation)
     */
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "verification_token", length = 100)
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    // Enum for User Category
    public enum UserCategory {
        MPP,
        STUDENT,
        NON_STUDENT
    }

    // Enum for User Status
    public enum UserStatus {
        PENDING,  // Waiting for approval
        ACTIVE,   // Approved and active
        BLOCKED   // Blocked by admin
    }
}