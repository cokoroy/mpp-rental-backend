package com.mpp.rental.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MPPApplicationResponse {

    private Integer applicationId;

    // Business info
    private Long businessId;
    private String businessName;
    private String businessCategory;
    private String businessDesc;
    private String businessStatus;

    // Owner info
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerCategory; // STUDENT / NON_STUDENT

    // Event info
    private Integer eventId;
    private String eventName;
    private String eventVenue;
    private String eventStartDate;
    private String eventEndDate;
    private String eventStatus;

    // Facility info
    private Integer eventFacilityId;
    private String facilityName;
    private String facilitySize;
    private String facilityType;

    // Application info
    private Integer applicationFacilityQuantity;
    private String applicationStatus;
    private LocalDateTime applicationCreatedAt;
    private String rejectionReason;

    // Payment info
    private Integer paymentId;
    private BigDecimal paymentAmount;
    private String paymentStatus; // UNPAID / PAID / FAILED / null (no payment)
}