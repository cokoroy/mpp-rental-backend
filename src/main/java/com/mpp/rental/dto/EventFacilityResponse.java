package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFacilityResponse {

    private Integer eventFacilityId;
    private Integer eventId;
    private Integer facilityId;
    private String facilityName;
    private String facilitySize;
    private String facilityType;
    private String facilityDesc;
    private String facilityUsage;
    private Integer quantityFacilityAvailable;
    private BigDecimal facilityStudentPrice;
    private BigDecimal facilityNonStudentPrice;
    private Integer maxPerBusiness;
}
