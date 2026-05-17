package com.mpp.rental.repository;

import com.mpp.rental.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Get all notifications for a specific user (via notification_read join)
     * Ordered newest first
     */
    @Query("""
            SELECT n FROM Notification n
            JOIN n.readRecords nr
            WHERE nr.user.userId = :userId
            ORDER BY n.createdAt DESC
            """)
    List<Notification> findAllByUserId(@Param("userId") Long userId);

    /**
     * Count unread notifications for a user
     */
    @Query("""
            SELECT COUNT(nr) FROM NotificationRead nr
            WHERE nr.user.userId = :userId
            AND nr.readAt IS NULL
            """)
    long countUnreadByUserId(@Param("userId") Long userId);
}