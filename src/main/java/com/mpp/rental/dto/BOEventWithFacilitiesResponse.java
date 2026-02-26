package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BOEventWithFacilitiesResponse {

    private Integer eventId;
    private String eventName;
    private String eventVenue;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private LocalTime eventStartTime;
    private LocalTime eventEndTime;
    private String eventType;
    private String eventDesc;
    private String eventApplicationStatus;
    private String eventStatus;
    private LocalDateTime eventCreateAt;
    private List<BOEventFacilityResponse> facilities;
}