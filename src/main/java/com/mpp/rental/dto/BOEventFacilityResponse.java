package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BOEventFacilityResponse {

    private Integer eventFacilityId;
    private Integer facilityId;
    private String facilityName;
    private String facilitySize;
    private String facilityType;
    private String facilityDesc;
    private String facilityUsage;
    private String facilityRemark;
    private String facilityImage;
    private Integer quantityFacilityAvailable;
    private BigDecimal facilityStudentPrice;
    private BigDecimal facilityNonStudentPrice;
    private BigDecimal applicablePrice;   // price based on current user's category
    private Integer maxPerBusiness;
    private Integer remainingQuota;       // maxPerBusiness - totalAppliedByThisBusiness (PENDING+APPROVED)
    private boolean hasPendingApplication; // block re-apply if true
}