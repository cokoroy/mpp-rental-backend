package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO returned to frontend for each notification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long notificationId;
    private String type;           // e.g. "APPLICATION_SUBMITTED"
    private String title;
    private String message;
    private Long referenceId;      // e.g. applicationId — for navigation
    private String referenceType;  // e.g. "APPLICATION"
    private boolean read;          // true if current user has read it
    private LocalDateTime createdAt;
}