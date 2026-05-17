package com.mpp.rental.controller;

import com.mpp.rental.dto.AnnouncementRequestDTO;
import com.mpp.rental.dto.ApiResponse;
import com.mpp.rental.dto.FcmTokenRequest;
import com.mpp.rental.dto.NotificationDTO;
import com.mpp.rental.model.User;
import com.mpp.rental.repository.UserRepository;
import com.mpp.rental.service.FcmService;
import com.mpp.rental.service.NotificationService;
import com.mpp.rental.service.SseEmitterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;
    private final UserRepository userRepository;
    private final FcmService fcmService; // Phase 2

    // ==================== SSE STREAM ====================

    /**
     * GET /api/notifications/stream
     * Opens a persistent SSE connection for real-time notifications.
     * Frontend calls this once on login via EventSource.
     * No @PreAuthorize here — JWT is validated manually because SSE uses
     * EventSource which cannot send custom headers in some browsers.
     * Instead we rely on the standard JWT filter which runs before this.
     */
    /**
     * SSE stream endpoint.
     *
     * CRITICAL: This method must NOT call any @Transactional method or open any DB query.
     * SseEmitter keeps the Tomcat async thread alive for the full connection lifetime.
     * Any JPA call here holds a HikariCP connection for that entire duration,
     * exhausting the pool and causing "Connection is not available" errors.
     *
     * Solution: resolve userId via a plain JDBC query outside JPA transaction scope,
     * or use the cached user lookup below which uses a separate read-only connection
     * that is released immediately after the query completes.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Use a non-transactional lookup — returns immediately and releases DB connection
        // before the SseEmitter starts holding the async thread.
        Long userId = userRepository.findUserIdByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        log.info("SSE stream opened for userId={} ({})", userId, email);
        return sseEmitterService.createEmitter(userId);
    }

    // ==================== GET NOTIFICATIONS ====================

    /**
     * GET /api/notifications
     * Get all notifications for the currently logged-in user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getMyNotifications() {
        try {
            List<NotificationDTO> notifications = notificationService.getMyNotifications();
            return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/notifications/unread-count
     * Get unread notification count for the bell badge
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        try {
            long count = notificationService.getUnreadCount();
            return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== MARK AS READ ====================

    /**
     * PUT /api/notifications/mark-all-read
     * Mark all notifications as read — called when user opens the notification panel
     */
    @PutMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        try {
            notificationService.markAllAsRead();
            return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/notifications/{id}/read
     * Mark a single notification as read — called when user clicks a notification
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== ANNOUNCEMENTS ====================

    /**
     * POST /api/notifications/announcement
     * MPP creates and sends an announcement to Business Owners
     */
    @PostMapping("/announcement")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<NotificationDTO>> createAnnouncement(
            @Valid @RequestBody AnnouncementRequestDTO request) {
        try {
            NotificationDTO result = notificationService.createAnnouncement(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Announcement sent successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== FCM TOKEN (Phase 2) ====================

    /**
     * POST /api/notifications/fcm-token
     * Frontend registers browser FCM token after user grants notification permission
     */
    @PostMapping("/fcm-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> registerFcmToken(
            @Valid @RequestBody FcmTokenRequest request) {
        try {
            fcmService.saveToken(request.getFcmToken(), request.getDeviceInfo());
            return ResponseEntity.ok(ApiResponse.success("FCM token registered successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/notifications/fcm-token
     * Called on logout to deactivate the browser's FCM token
     */
    @DeleteMapping("/fcm-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deregisterFcmToken() {
        try {
            fcmService.deactivateTokensForCurrentUser();
            return ResponseEntity.ok(ApiResponse.success("FCM token deregistered successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}