package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class FacilityUtilisationRow {
    private Integer    facilityId;
    private String     facilityName;
    private String     facilityType;
    private String     facilitySize;
    private int        timesOffered;           // number of events this facility was assigned to
    private int        totalSlotsOffered;      // sum of originalQuantityTotal across all assignments
    private int        totalSlotsFilled;       // sum of (original - current) across all assignments
    private int        fillRate;               // slotsFilled / slotsOffered * 100
    private int        totalApplications;
    private int        totalApproved;
    private BigDecimal totalRevenueGenerated;  // sum of PAID payments for this facility
}