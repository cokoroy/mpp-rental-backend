package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketResponse {

    private Integer ticketId;

    // Submitter info
    private Long userId;
    private String userName;
    private String userEmail;
    private String userCategory;

    // Ticket info
    private String ticketTitle;
    private String ticketDescription;
    private String ticketCategory;
    private String ticketPriority;
    private String ticketStatus;

    // Responses (null when listing, populated when viewing detail)
    private List<TicketResponseDto> responses;

    // Feedback (null = no feedback submitted yet)
    private Integer feedbackRating;
    private String feedbackComment;
    private LocalDateTime feedbackSubmittedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}