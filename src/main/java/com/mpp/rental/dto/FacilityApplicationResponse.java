package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityApplicationResponse {

    private Integer applicationId;

    // Business info
    private Long businessId;
    private String businessName;

    // Event info
    private Integer eventId;
    private String eventName;
    private String eventVenue;

    // Facility info
    private Integer eventFacilityId;
    private String facilityName;
    private String facilitySize;

    // Application info
    private Integer applicationFacilityQuantity;
    private String applicationStatus;
    private LocalDateTime applicationCreatedAt;
    private String rejectionReason;

    // Payment info (null if no payment record)
    private Integer paymentId;
    private BigDecimal paymentAmount;
    private String paymentStatus;
}