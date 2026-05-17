package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class EventPerformanceSummary {
    private int        totalEvents;
    private int        totalFullyBookedEvents;  // events with 0 remaining quota
    private int        totalApplications;
    private BigDecimal totalRevenue;
    private int        avgApplicationsPerEvent; // rounded
    private String     bestPerformingEvent;     // event name with highest revenue
}