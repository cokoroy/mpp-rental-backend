package com.mpp.rental.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "BUSINESS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "businessId")
    private Long businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(name = "businessName", nullable = false, unique = true, length = 255)
    private String businessName;

    @Column(name = "ssmNumber", unique = true, length = 255)
    private String ssmNumber;

    @Column(name = "businessCategory", nullable = false, length = 255)
    private String businessCategory;

    @Column(name = "businessDesc", length = 255)
    private String businessDesc;

    @Column(name = "businessStatus", nullable = false, length = 255)
    @Builder.Default
    private String businessStatus = "ACTIVE";

    @CreationTimestamp
    @Column(name = "businessRegisteredAt", nullable = false, updatable = false)
    private LocalDateTime businessRegisteredAt;

    @Column(name = "ssmDocument", length = 255)
    private String ssmDocument;
}
