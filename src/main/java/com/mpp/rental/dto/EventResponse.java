package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Integer eventId;
    private String eventName;
    private String eventVenue;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private LocalTime eventStartTime;
    private LocalTime eventEndTime;
    private String eventType;
    private String eventDesc;
    private String eventApplicationStatus;  // OPEN or CLOSED
    private String eventStatus;             // upcoming, active, completed, cancelled
    private LocalDateTime eventCreateAt;
}
