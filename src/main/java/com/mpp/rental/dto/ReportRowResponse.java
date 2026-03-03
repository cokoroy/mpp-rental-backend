package com.mpp.rental.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRowResponse {

    private Integer       applicationId;

    // Event
    private String        eventName;
    private String        eventVenue;
    private String        eventStatus;

    // Facility
    private String        facilityName;

    // Business
    private Long          businessId;
    private String        businessName;

    // Owner
    private Long          ownerId;
    private String        ownerName;
    private String        ownerCategory;   // STUDENT | NON_STUDENT

    // Application
    private String        applicationStatus;
    private LocalDateTime applicationCreatedAt;

    // Payment
    private String        paymentStatus;   // PAID | UNPAID | FAILED | null if no payment
    private BigDecimal    paymentAmount;
}