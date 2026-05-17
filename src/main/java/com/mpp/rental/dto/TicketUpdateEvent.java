package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TicketUpdateEvent - Payload broadcast over WebSocket to all subscribers
 * of /topic/ticket/{ticketId} when a new reply is added or status changes.
 *
 * Frontend receives this and updates the UI instantly without polling.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketUpdateEvent {

    /**
     * Type of event:
     * NEW_REPLY    - a new response was added to the thread
     * STATUS_CHANGE - ticket status was updated (resolved, reopened)
     * PRIORITY_CHANGE - ticket priority was updated by MPP
     */
    private String eventType;

    /**
     * Full updated ticket with all responses
     * Frontend replaces its local state with this
     */
    private SupportTicketResponse ticket;
}