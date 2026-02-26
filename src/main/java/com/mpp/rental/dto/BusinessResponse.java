package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessResponse {

    private Long businessId;
    private String businessName;
    private String ssmNumber;
    private String businessCategory;
    private String businessDesc;
    private String businessStatus;
    private LocalDateTime businessRegisteredAt;
    private String ssmDocument;

    // Owner information
    private Long userId;
    private String ownerName;
    private String ownerEmail;
    private String ownerCategory;
    private String ownerPhoneNumber;
}
