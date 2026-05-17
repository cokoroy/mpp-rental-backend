package com.mpp.rental.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks which users have read which notifications.
 * Shared model: one Notification row, one NotificationRead row per recipient.
 *
 * Example: MPP creates event → one Notification row created
 *          → one NotificationRead row per Business Owner (all unread initially)
 *          → when BO opens panel → their NotificationRead.readAt is set
 */
@Entity
@Table(name = "notification_read",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"notification_id", "user_id"}
        ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The notification this record belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    /**
     * The user who is a recipient of this notification
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Null = unread. Set to current time when user reads it.
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    // ==================== HELPER ====================

    public boolean isRead() {
        return readAt != null;
    }
}