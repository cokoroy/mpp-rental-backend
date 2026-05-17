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

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private String userPassword;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Business> businesses;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_category", nullable = false, length = 50)
    private UserCategory userCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 50)
    private UserStatus userStatus = UserStatus.PENDING;

    // ── Address fields (replaces the old single user_address column) ──────────
    @Column(name = "user_address_line1", length = 255)
    private String userAddressLine1;

    @Column(name = "user_address_line2", length = 255)
    private String userAddressLine2;

    @Column(name = "user_city", length = 100)
    private String userCity;

    @Column(name = "user_postal_code", length = 10)
    private String userPostalCode;

    @Column(name = "user_state", length = 100)
    private String userState;
    // ─────────────────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "user_registered_at", nullable = false, updatable = false)
    private LocalDateTime userRegisteredAt;

    @Column(name = "user_last_login")
    private LocalDateTime userLastLogin;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private BankAccount bankAccount;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "verification_token", length = 100)
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    public enum UserCategory {
        MPP,
        STUDENT,
        NON_STUDENT,
        SUPER_ADMIN
    }

    public enum UserStatus {
        PENDING,
        ACTIVE,
        BLOCKED
    }
}