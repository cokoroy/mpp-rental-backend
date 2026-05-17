package com.mpp.rental.service;

import com.google.firebase.messaging.*;
import com.mpp.rental.model.FcmToken;
import com.mpp.rental.model.User;
import com.mpp.rental.repository.FcmTokenRepository;
import com.mpp.rental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * FcmService — Phase 2: sends browser push notifications via Firebase Cloud Messaging.
 * Called by NotificationService when target user is NOT online (no active SSE connection).
 *
 * Flow:
 * 1. User logs in → frontend requests browser notification permission
 * 2. Firebase gives browser a unique FCM token
 * 3. Frontend calls POST /api/notifications/fcm-token to save it here
 * 4. When user is offline, NotificationService calls FcmService.sendPush()
 * 5. Firebase pushes browser notification to user's device
 * 6. User clicks notification → browser opens, navigates to correct page
 * 7. On logout → frontend calls DELETE /api/notifications/fcm-token
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    // ==================== TOKEN MANAGEMENT ====================

    /**
     * Save or update FCM token for the currently logged-in user.
     * Called when frontend registers a browser token after permission is granted.
     * If token already exists, just reactivate it.
     */
    @Transactional
    public void saveToken(String fcmToken, String deviceInfo) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if token already exists — update it instead of creating duplicate
        fcmTokenRepository.findByFcmToken(fcmToken).ifPresentOrElse(
                existing -> {
                    existing.setIsActive(true);
                    existing.setDeviceInfo(deviceInfo);
                    fcmTokenRepository.save(existing);
                    log.info("FCM token reactivated for userId={}", user.getUserId());
                },
                () -> {
                    FcmToken token = new FcmToken();
                    token.setUser(user);
                    token.setFcmToken(fcmToken);
                    token.setDeviceInfo(deviceInfo);
                    token.setIsActive(true);
                    fcmTokenRepository.save(token);
                    log.info("FCM token saved for userId={}", user.getUserId());
                }
        );
    }

    /**
     * Called on logout — we do NOT deactivate the token here.
     * Keeping the token active allows FCM to still push to this browser
     * even after the user has logged out (which is the whole point of Phase 2).
     *
     * Tokens are only deactivated when:
     * - FCM returns UNREGISTERED or INVALID_ARGUMENT (token expired/revoked)
     * - A new login registers a fresh token for the same browser (handled in saveToken)
     *
     * This method is kept for API compatibility but is now a no-op.
     */
    @Transactional
    public void deactivateTokensForCurrentUser() {
        // Intentionally left empty — see Javadoc above
        log.debug("FCM token deactivation skipped on logout (tokens stay active for offline push)");
    }

    // ==================== SEND PUSH ====================

    /**
     * Send FCM push notification to a single user (all their active devices).
     * Called by NotificationService when user is offline (no SSE connection).
     */
    public void sendPush(Long userId, String title, String body, String referenceType, Long referenceId) {
        List<FcmToken> tokens = fcmTokenRepository.findByUser_UserIdAndIsActiveTrue(userId);

        if (tokens.isEmpty()) {
            log.debug("No active FCM tokens for userId={}, skipping push", userId);
            return;
        }

        for (FcmToken tokenRecord : tokens) {
            try {
                Message message = Message.builder()
                        .setToken(tokenRecord.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        // Pass extra data so frontend knows where to navigate on click
                        .putData("referenceType", referenceType != null ? referenceType : "")
                        .putData("referenceId", referenceId != null ? referenceId.toString() : "")
                        .putData("userId", userId.toString())
                        // Web push config — shows notification even when tab is closed
                        .setWebpushConfig(WebpushConfig.builder()
                                .setNotification(WebpushNotification.builder()
                                        .setTitle(title)
                                        .setBody(body)
                                        .setIcon("/favicon.ico")
                                        .setBadge("/favicon.ico")
                                        .build())
                                .setFcmOptions(WebpushFcmOptions.builder()
                                        .setLink("/notifications")
                                        .build())
                                .build())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("FCM push sent to userId={}, messageId={}", userId, response);

            } catch (FirebaseMessagingException e) {
                log.warn("FCM push failed for userId={}, token={}: {}",
                        userId, tokenRecord.getFcmToken().substring(0, 20) + "...", e.getMessage());

                // If token is invalid/expired, deactivate it
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                        || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                    tokenRecord.setIsActive(false);
                    fcmTokenRepository.save(tokenRecord);
                    log.info("Deactivated invalid FCM token for userId={}", userId);
                }
            }
        }
    }

    /**
     * Send FCM push to multiple users at once (e.g. all MPP users).
     */
    public void sendPushToMany(List<Long> userIds, String title, String body,
                               String referenceType, Long referenceId) {
        for (Long userId : userIds) {
            try {
                sendPush(userId, title, body, referenceType, referenceId);
            } catch (Exception e) {
                log.warn("Failed to send FCM to userId={}: {}", userId, e.getMessage());
            }
        }
    }
}