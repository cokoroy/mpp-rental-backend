package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityResponse {

    private Integer facilityId;
    private String facilityName;
    private String facilitySize;
    private String facilityType;
    private String facilityDesc;
    private String usage;
    private String remark;
    private String facilityImage;
    private BigDecimal facilityBaseStudentPrice;
    private BigDecimal facilityBaseNonstudentPrice;
    private String facilityStatus;
    private LocalDateTime facilityCreateAt;
}