package com.mpp.rental.service;

import com.mpp.rental.dto.AnnouncementRequestDTO;
import com.mpp.rental.dto.NotificationDTO;
import com.mpp.rental.model.*;
import com.mpp.rental.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final UserRepository userRepository;
    private final SseEmitterService sseEmitterService;
    private final FcmService fcmService; // Phase 2

    // ==================== CORE SEND METHOD ====================

    /**
     * Main method called by all other services to send a notification.
     * Phase 1: saves to DB + pushes via SSE
     * Phase 2: also pushes via FCM if user is offline (inject FcmService here)
     */
    @Transactional
    public void sendNotification(
            Notification.NotificationType type,
            String title,
            String message,
            Long referenceId,
            String referenceType,
            List<Long> recipientUserIds) {

        // Step 1: Save notification to DB
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);
        Notification saved = notificationRepository.save(notification);

        // Step 2: Create NotificationRead records for each recipient (all unread)
        List<User> recipients = userRepository.findAllById(recipientUserIds);
        for (User recipient : recipients) {
            NotificationRead readRecord = new NotificationRead();
            readRecord.setNotification(saved);
            readRecord.setUser(recipient);
            readRecord.setReadAt(null); // unread
            notificationReadRepository.save(readRecord);
        }

        // Step 3: Push to each recipient
        NotificationDTO dto = mapToDTO(saved, false);
        for (Long userId : recipientUserIds) {
            // Always try SSE (instant in-app update if tab is open)
            sseEmitterService.sendToUser(userId, dto);

            // Always try FCM (OS popup — works even when tab is closed)
            // When tab IS open: Firebase foreground handler receives it (no OS popup shown)
            // When tab is NOT open: Service worker shows OS popup
            try {
                fcmService.sendPush(userId, title, message, referenceType, referenceId);
            } catch (Exception e) {
                log.warn("FCM push failed for userId={}: {}", userId, e.getMessage());
            }
        }

        log.info("Notification [{}] sent to {} recipients", type, recipientUserIds.size());
    }

    // ==================== CONVENIENCE METHODS (called by other services) ====================

    /**
     * Notify MPP when Business Owner submits a facility application
     */
    @Transactional
    public void notifyApplicationSubmitted(Long applicationId, String businessName, String eventName) {
        List<Long> mppUserIds = userRepository.findMPPUserIds();
        sendNotification(
                Notification.NotificationType.APPLICATION_SUBMITTED,
                "New Facility Application",
                businessName + " submitted an application for " + eventName,
                applicationId,
                "APPLICATION",
                mppUserIds
        );
    }

    /**
     * Notify Business Owner when their application is approved
     */
    @Transactional
    public void notifyApplicationApproved(Long applicationId, Long businessOwnerUserId,
                                          String facilityName, String eventName) {
        sendNotification(
                Notification.NotificationType.APPLICATION_APPROVED,
                "Application Approved",
                "Your application for " + facilityName + " in " + eventName + " has been approved.",
                applicationId,
                "APPLICATION",
                List.of(businessOwnerUserId)
        );
    }

    /**
     * Notify Business Owner when their application is rejected
     */
    @Transactional
    public void notifyApplicationRejected(Long applicationId, Long businessOwnerUserId,
                                          String facilityName, String eventName, String reason) {
        String msg = "Your application for " + facilityName + " in " + eventName + " has been rejected.";
        if (reason != null && !reason.isBlank()) {
            msg += " Reason: " + reason;
        }
        sendNotification(
                Notification.NotificationType.APPLICATION_REJECTED,
                "Application Rejected",
                msg,
                applicationId,
                "APPLICATION",
                List.of(businessOwnerUserId)
        );
    }

    /**
     * Notify Business Owner when their application is auto-rejected (quota filled)
     */
    @Transactional
    public void notifyApplicationAutoRejected(Long applicationId, Long businessOwnerUserId,
                                              String facilityName, String eventName) {
        sendNotification(
                Notification.NotificationType.APPLICATION_AUTO_REJECTED,
                "Application Auto-Rejected",
                "Your application for " + facilityName + " in " + eventName +
                        " was rejected because the facility quota has been filled.",
                applicationId,
                "APPLICATION",
                List.of(businessOwnerUserId)
        );
    }

    /**
     * Notify MPP when Business Owner confirms payment
     */
    @Transactional
    public void notifyPaymentConfirmed(Long applicationId, String businessName, String facilityName) {
        List<Long> mppUserIds = userRepository.findMPPUserIds();
        sendNotification(
                Notification.NotificationType.PAYMENT_CONFIRMED,
                "Payment Confirmed",
                businessName + " has confirmed payment for " + facilityName + ".",
                applicationId,
                "PAYMENT",
                mppUserIds
        );
    }

    /**
     * Notify MPP when Business Owner submits a support ticket
     */
    @Transactional
    public void notifySupportTicketCreated(Long ticketId, String userName, String ticketTitle) {
        List<Long> mppUserIds = userRepository.findMPPUserIds();
        sendNotification(
                Notification.NotificationType.SUPPORT_TICKET_CREATED,
                "New Support Ticket",
                userName + " submitted a ticket: \"" + ticketTitle + "\"",
                ticketId,
                "TICKET",
                mppUserIds
        );
    }

    /**
     * Notify Business Owner when MPP replies to their ticket
     * Called by mppReplyToTicket()
     */
    @Transactional
    public void notifySupportTicketReplied(Long ticketId, Long businessOwnerUserId, String ticketTitle) {
        sendNotification(
                Notification.NotificationType.SUPPORT_TICKET_REPLIED,
                "Support Ticket Reply",
                "MPP has replied to your ticket: \"" + ticketTitle + "\"",
                ticketId,
                "TICKET",
                List.of(businessOwnerUserId)
        );
    }

    /**
     * Notify MPP when Business Owner replies to an existing ticket
     * Called by replyToTicket() — passes null as boUserId to route to MPP
     */
    @Transactional
    public void notifySupportTicketBoReplied(Long ticketId, String boName, String ticketTitle) {
        List<Long> mppUserIds = userRepository.findMPPUserIds();
        sendNotification(
                Notification.NotificationType.SUPPORT_TICKET_REPLIED,
                "Support Ticket Update",
                boName + " replied to ticket: \"" + ticketTitle + "\"",
                ticketId,
                "TICKET",
                mppUserIds
        );
    }

    /**
     * Notify all Business Owners when a new event is created
     */
    @Transactional
    public void notifyEventCreated(Long eventId, String eventName) {
        List<Long> boUserIds = userRepository.findBusinessOwnerUserIds();
        if (boUserIds.isEmpty()) return;
        sendNotification(
                Notification.NotificationType.EVENT_CREATED,
                "New Event Available",
                "A new event \"" + eventName + "\" is now open for applications.",
                eventId,
                "EVENT",
                boUserIds
        );
    }

    /**
     * MPP creates an announcement — notifies target Business Owners
     */
    @Transactional
    public NotificationDTO createAnnouncement(AnnouncementRequestDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User mpp = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Determine recipients based on target audience
        List<Long> recipientIds;
        String audience = request.getTargetAudience() == null ? "ALL" : request.getTargetAudience().toUpperCase();
        switch (audience) {
            case "STUDENT" -> recipientIds = userRepository.findStudentUserIds();
            case "NON_STUDENT" -> recipientIds = userRepository.findNonStudentUserIds();
            default -> recipientIds = userRepository.findBusinessOwnerUserIds();
        }

        // Save notification
        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.ANNOUNCEMENT);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setReferenceId(request.getReferenceId());
        notification.setReferenceType("ANNOUNCEMENT");
        notification.setCreatedBy(mpp);
        Notification saved = notificationRepository.save(notification);

        // Create read records for each recipient
        List<User> recipients = userRepository.findAllById(recipientIds);
        for (User recipient : recipients) {
            NotificationRead readRecord = new NotificationRead();
            readRecord.setNotification(saved);
            readRecord.setUser(recipient);
            notificationReadRepository.save(readRecord);
        }

        // Push via SSE to online users
        NotificationDTO dto = mapToDTO(saved, false);
        for (Long userId : recipientIds) {
            sseEmitterService.sendToUser(userId, dto);
        }

        log.info("Announcement created and sent to {} recipients (audience={})",
                recipientIds.size(), audience);
        return dto;
    }

    // ==================== QUERY METHODS ====================

    /**
     * Get all notifications for the currently logged-in user
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getMyNotifications() {
        Long userId = getCurrentUserId();
        List<Notification> notifications = notificationRepository.findAllByUserId(userId);

        return notifications.stream()
                .map(n -> {
                    boolean isRead = n.getReadRecords().stream()
                            .anyMatch(nr -> nr.getUser().getUserId().equals(userId) && nr.isRead());
                    return mapToDTO(n, isRead);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get unread notification count for badge
     */
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Long userId = getCurrentUserId();
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * Mark all notifications as read for the current user (when panel opens)
     */
    @Transactional
    public void markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationReadRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("All notifications marked as read for userId={}", userId);
    }

    /**
     * Mark a single notification as read (when user clicks it)
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Long userId = getCurrentUserId();
        notificationReadRepository
                .findByNotification_NotificationIdAndUser_UserId(notificationId, userId)
                .ifPresent(nr -> {
                    if (!nr.isRead()) {
                        nr.setReadAt(LocalDateTime.now());
                        notificationReadRepository.save(nr);
                    }
                });
    }

    /**
     * Permanently delete all notification_read records for the current user.
     * The notification row itself stays (other users may still have read records).
     */
    @Transactional
    public void clearAllForCurrentUser() {
        Long userId = getCurrentUserId();
        notificationReadRepository.deleteAllByUserId(userId);
        log.info("All notifications cleared for userId={}", userId);
    }

    // ==================== HELPERS ====================

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUserId();
    }

    private NotificationDTO mapToDTO(Notification notification, boolean isRead) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(notification.getNotificationId());
        dto.setType(notification.getType().name());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setReferenceId(notification.getReferenceId());
        dto.setReferenceType(notification.getReferenceType());
        dto.setRead(isRead);
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}