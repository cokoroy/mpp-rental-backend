package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class FacilityRentalRow {
    private Integer       applicationId;
    private String        eventName;
    private String        eventVenue;
    private String        eventStatus;
    private String        facilityName;
    private String        facilityType;
    private Long          businessId;
    private String        businessName;
    private Long          ownerId;
    private String        ownerName;
    private String        ownerCategory;        // STUDENT | NON_STUDENT
    private String        applicationStatus;
    private LocalDateTime applicationCreatedAt;
    private String        paymentStatus;         // PAID | UNPAID | FAILED | null
    private BigDecimal    paymentAmount;
}