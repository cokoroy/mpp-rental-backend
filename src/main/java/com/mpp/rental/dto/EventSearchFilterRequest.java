package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventSearchFilterRequest {
    
    private String searchQuery;      // Search by event name
    private String eventStatus;      // Filter by status: all, upcoming, active, completed, cancelled
}
