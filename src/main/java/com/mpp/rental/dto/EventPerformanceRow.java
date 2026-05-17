package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class EventPerformanceRow {
    private Integer    eventId;
    private String     eventName;
    private String     eventVenue;
    private String     eventStatus;
    private LocalDate  eventStartDate;
    private LocalDate  eventEndDate;
    private int        totalFacilitiesOffered;  // distinct facility types assigned
    private int        totalSlotsAvailable;     // sum of originalQuantityTotal across all event facilities
    private int        totalSlotsFilled;        // totalSlotsAvailable - sum of current quantityFacilityAvailable
    private int        fillRate;                // slotsFilled / slotsAvailable * 100
    private int        totalApplications;
    private int        totalApproved;
    private int        totalRejected;
    private BigDecimal totalRevenue;            // sum of PAID payments
}