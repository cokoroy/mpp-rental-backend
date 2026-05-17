package com.mpp.rental.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    /**
     * Type of notification — determines icon and routing on frontend
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    /**
     * ID of the related entity (e.g. applicationId, ticketId, eventId)
     * Used by frontend to navigate to the correct page on click
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * Type of the related entity — helps frontend decide where to navigate
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * User who triggered this notification (null for system-generated)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Which users should receive this notification
     * Shared model: one notification, many readers tracked in notification_read
     */
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationRead> readRecords;

    // ==================== ENUMS ====================

    public enum NotificationType {
        // MPP receives these
        APPLICATION_SUBMITTED,
        PAYMENT_CONFIRMED,
        SUPPORT_TICKET_CREATED,

        // Business Owner receives these
        APPLICATION_APPROVED,
        APPLICATION_REJECTED,
        APPLICATION_AUTO_REJECTED,
        SUPPORT_TICKET_REPLIED,

        // All Business Owners receive these
        EVENT_CREATED,
        ANNOUNCEMENT
    }
}