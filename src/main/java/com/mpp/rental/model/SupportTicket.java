package com.mpp.rental.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "SUPPORT_TICKET")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Integer ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ticket_title", nullable = false, length = 255)
    private String ticketTitle;

    @Column(name = "ticket_description", nullable = false, length = 1000)
    private String ticketDescription;

    /**
     * Ticket Category: PAYMENT, TECHNICAL, APPLICATION, GENERAL
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_category", nullable = false, length = 50)
    private TicketCategory ticketCategory;

    /**
     * Ticket Priority: LOW, MEDIUM, HIGH
     * Set by Business Owner on creation, can be updated by MPP
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_priority", nullable = false, length = 50)
    private TicketPriority ticketPriority = TicketPriority.LOW;

    /**
     * Ticket Status: OPEN, IN_PROGRESS, RESOLVED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false, length = 50)
    private TicketStatus ticketStatus = TicketStatus.OPEN;

    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketResponse> responses;

    /**
     * Feedback — submitted by Business Owner after ticket is RESOLVED
     * Rating: 1-5 stars (null = no feedback yet)
     * Comment: optional written feedback
     */
    @Column(name = "feedback_rating")
    private Integer feedbackRating;

    @Column(name = "feedback_comment", length = 1000)
    private String feedbackComment;

    @Column(name = "feedback_submitted_at")
    private LocalDateTime feedbackSubmittedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TicketCategory {
        PAYMENT,
        TECHNICAL,
        APPLICATION,
        GENERAL
    }

    public enum TicketPriority {
        LOW,
        MEDIUM,
        HIGH
    }

    public enum TicketStatus {
        OPEN,
        IN_PROGRESS,
        RESOLVED
    }
}