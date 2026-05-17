package com.mpp.rental.repository;

import com.mpp.rental.model.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {

    /**
     * Find a specific read record for a user + notification
     */
    Optional<NotificationRead> findByNotification_NotificationIdAndUser_UserId(
            Long notificationId, Long userId);

    /**
     * Get all unread notification records for a user
     */
    List<NotificationRead> findByUser_UserIdAndReadAtIsNull(Long userId);

    /**
     * Mark ALL unread notifications as read for a user (when panel opens)
     */
    @Modifying
    @Query("""
            UPDATE NotificationRead nr
            SET nr.readAt = :readAt
            WHERE nr.user.userId = :userId
            AND nr.readAt IS NULL
            """)
    void markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Permanently delete all notification_read records for a user (clear all)
     */
    @Modifying
    @Query("DELETE FROM NotificationRead nr WHERE nr.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}