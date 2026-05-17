package com.mpp.rental.repository;

import com.mpp.rental.model.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    /**
     * Get all active FCM tokens for a user (may have multiple devices/browsers)
     */
    List<FcmToken> findByUser_UserIdAndIsActiveTrue(Long userId);

    /**
     * Find token by its FCM token string (to check for duplicates)
     */
    Optional<FcmToken> findByFcmToken(String fcmToken);

    /**
     * Deactivate all tokens for a user on logout
     */
    @Modifying
    @Query("UPDATE FcmToken f SET f.isActive = false WHERE f.user.userId = :userId")
    void deactivateAllByUserId(@Param("userId") Long userId);

    /**
     * Get all active tokens for a list of user IDs (bulk push)
     */
    @Query("SELECT f FROM FcmToken f WHERE f.user.userId IN :userIds AND f.isActive = true")
    List<FcmToken> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);
}