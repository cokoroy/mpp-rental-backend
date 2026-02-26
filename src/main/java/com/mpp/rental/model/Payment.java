package com.mpp.rental.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentId")
    private Integer paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicationId", nullable = false)
    private FacilityApplication application;

    @Column(name = "paymentAmount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    /**
     * Payment Status: UNPAID, PAID, FAILED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "paymentStatus", nullable = false, length = 50)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @CreationTimestamp
    @Column(name = "paymentCreatedAt", nullable = false, updatable = false)
    private LocalDateTime paymentCreatedAt;

    public enum PaymentStatus {
        UNPAID,
        PAID,
        FAILED
    }
}