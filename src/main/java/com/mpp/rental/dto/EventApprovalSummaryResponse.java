package com.mpp.rental.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventApprovalSummaryResponse {

    private Integer eventId;
    private String eventName;
    private String eventVenue;
    private String eventStartDate;
    private String eventEndDate;
    private String eventStatus;
    private String eventApplicationStatus;

    // Application counts
    private int totalApplications;
    private int pendingCount;
    private int approvedCount;
    private int rejectedCount;
}