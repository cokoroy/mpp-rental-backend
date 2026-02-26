package com.mpp.rental.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "FACILITY_APPLICATION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applicationId")
    private Integer applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "businessId", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventFacilityId", nullable = false)
    private EventFacility eventFacility;

    @Column(name = "applicationFacilityQuantity", nullable = false)
    private Integer applicationFacilityQuantity;

    /**
     * Application Status: PENDING, APPROVED, REJECTED, CANCELLED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "applicationStatus", nullable = false, length = 50)
    private ApplicationStatus applicationStatus = ApplicationStatus.PENDING;

    @CreationTimestamp
    @Column(name = "applicationCreatedAt", nullable = false, updatable = false)
    private LocalDateTime applicationCreatedAt;

    @Column(name = "rejectionReason", length = 255)
    private String rejectionReason;

    public enum ApplicationStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED
    }
}