package com.mpp.rental.repository;

import com.mpp.rental.model.EventFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventFacilityRepository extends JpaRepository<EventFacility, Integer> {

    /**
     * Find all facilities assigned to an event
     */
    @Query("SELECT ef FROM EventFacility ef " +
            "JOIN FETCH ef.facility f " +
            "WHERE ef.event.eventId = :eventId " +
            "AND f.deletedAt IS NULL")
    List<EventFacility> findByEventIdWithFacility(@Param("eventId") Integer eventId);

    /**
     * Find specific facility assignment for an event
     */
    @Query("SELECT ef FROM EventFacility ef " +
            "WHERE ef.event.eventId = :eventId " +
            "AND ef.facility.facilityId = :facilityId")
    Optional<EventFacility> findByEventIdAndFacilityId(
            @Param("eventId") Integer eventId,
            @Param("facilityId") Integer facilityId
    );

    /**
     * Check if a facility assignment has any applications
     * Used to prevent deletion of facilities with existing applications
     */
    @Query("SELECT CASE WHEN COUNT(fa) > 0 THEN true ELSE false END " +
            "FROM FacilityApplication fa " +
            "WHERE fa.eventFacility.eventFacilityId = :eventFacilityId")
    boolean hasApplications(@Param("eventFacilityId") Integer eventFacilityId);

    /**
     * Delete all facility assignments for an event
     */
    void deleteByEvent_EventId(Integer eventId);

    /**
     * Count facilities assigned to an event
     */
    long countByEvent_EventId(Integer eventId);
}