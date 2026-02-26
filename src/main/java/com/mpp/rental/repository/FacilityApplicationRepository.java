package com.mpp.rental.repository;

import com.mpp.rental.model.FacilityApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityApplicationRepository extends JpaRepository<FacilityApplication, Integer> {

    /**
     * Get total quantity applied by a business for a specific eventFacility
     * Only counts PENDING and APPROVED (not REJECTED or CANCELLED)
     */
    @Query("SELECT COALESCE(SUM(fa.applicationFacilityQuantity), 0) FROM FacilityApplication fa " +
            "WHERE fa.business.businessId = :businessId " +
            "AND fa.eventFacility.eventFacilityId = :eventFacilityId " +
            "AND fa.applicationStatus IN ('PENDING', 'APPROVED')")
    Integer getTotalAppliedQuantity(
            @Param("businessId") Long businessId,
            @Param("eventFacilityId") Integer eventFacilityId
    );

    /**
     * Check if a business has a PENDING application for a specific eventFacility
     */
    @Query("SELECT CASE WHEN COUNT(fa) > 0 THEN true ELSE false END FROM FacilityApplication fa " +
            "WHERE fa.business.businessId = :businessId " +
            "AND fa.eventFacility.eventFacilityId = :eventFacilityId " +
            "AND fa.applicationStatus = 'PENDING'")
    boolean hasPendingApplication(
            @Param("businessId") Long businessId,
            @Param("eventFacilityId") Integer eventFacilityId
    );

    /**
     * Check if an eventFacility has any applications (used in EventFacilityRepository.hasApplications)
     */
    @Query("SELECT CASE WHEN COUNT(fa) > 0 THEN true ELSE false END FROM FacilityApplication fa " +
            "WHERE fa.eventFacility.eventFacilityId = :eventFacilityId")
    boolean existsByEventFacilityId(@Param("eventFacilityId") Integer eventFacilityId);

    /**
     * Find all applications by business owner (via business userId)
     */
    @Query("SELECT fa FROM FacilityApplication fa " +
            "JOIN FETCH fa.business b " +
            "JOIN FETCH fa.eventFacility ef " +
            "JOIN FETCH ef.event e " +
            "JOIN FETCH ef.facility f " +
            "WHERE b.user.userId = :userId " +
            "ORDER BY fa.applicationCreatedAt DESC")
    List<FacilityApplication> findAllByUserId(@Param("userId") Long userId);

    // ==================== MPP APPROVAL QUERIES ====================

    /**
     * Find all applications for a specific event (for MPP approval page)
     */
    @Query("SELECT fa FROM FacilityApplication fa " +
            "JOIN FETCH fa.business b " +
            "JOIN FETCH b.user u " +
            "JOIN FETCH fa.eventFacility ef " +
            "JOIN FETCH ef.event e " +
            "JOIN FETCH ef.facility f " +
            "WHERE e.eventId = :eventId " +
            "ORDER BY fa.applicationCreatedAt DESC")
    List<FacilityApplication> findAllByEventIdWithDetails(@Param("eventId") Integer eventId);

    /**
     * Find all applications for a specific event (simple, for counting)
     */
    @Query("SELECT fa FROM FacilityApplication fa " +
            "JOIN fa.eventFacility ef " +
            "WHERE ef.event.eventId = :eventId")
    List<FacilityApplication> findAllByEventId(@Param("eventId") Integer eventId);

    /**
     * Find a single application with all details eagerly loaded
     */
    @Query("SELECT fa FROM FacilityApplication fa " +
            "JOIN FETCH fa.business b " +
            "JOIN FETCH b.user u " +
            "JOIN FETCH fa.eventFacility ef " +
            "JOIN FETCH ef.event e " +
            "JOIN FETCH ef.facility f " +
            "WHERE fa.applicationId = :applicationId")
    Optional<FacilityApplication> findByIdWithDetails(@Param("applicationId") Integer applicationId);

    /**
     * Find all PENDING applications for a specific eventFacility
     * Used for auto-reject when quota hits 0
     */
    @Query("SELECT fa FROM FacilityApplication fa " +
            "WHERE fa.eventFacility.eventFacilityId = :eventFacilityId " +
            "AND fa.applicationStatus = 'PENDING'")
    List<FacilityApplication> findPendingByEventFacilityId(@Param("eventFacilityId") Integer eventFacilityId);

    /**
     * Find all PENDING applications for a blocked user
     * Used for auto-reject when business owner is blocked
     */
    @Query("SELECT fa FROM FacilityApplication fa " +
            "JOIN fa.business b " +
            "WHERE b.user.userId = :userId " +
            "AND fa.applicationStatus = 'PENDING'")
    List<FacilityApplication> findPendingByUserId(@Param("userId") Long userId);
}
