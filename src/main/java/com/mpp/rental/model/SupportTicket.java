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
     * OPEN: newly created by Business Owner
     * IN_PROGRESS: MPP has replied
     * RESOLVED: MPP marked as resolved
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false, length = 50)
    private TicketStatus ticketStatus = TicketStatus.OPEN;

    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketResponse> responses;

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