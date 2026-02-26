package com.mpp.rental.repository;

import com.mpp.rental.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {

    /**
     * Find event by ID excluding soft-deleted
     */
    Optional<Event> findByEventIdAndDeletedAtIsNull(Integer eventId);

    /**
     * Check if event name exists (case-insensitive, excluding soft-deleted)
     */
    boolean existsByEventNameIgnoreCaseAndDeletedAtIsNull(String eventName);

    /**
     * Check if event name exists excluding current event (for update)
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e " +
           "WHERE LOWER(e.eventName) = LOWER(:eventName) " +
           "AND e.eventId != :eventId " +
           "AND e.deletedAt IS NULL")
    boolean existsByEventNameAndNotId(@Param("eventName") String eventName, @Param("eventId") Integer eventId);

    /**
     * Find all events by status
     */
    List<Event> findByEventStatusAndDeletedAtIsNull(String eventStatus);

    /**
     * Find events that need status update (upcoming -> active, active -> completed)
     */
    @Query("SELECT e FROM Event e WHERE e.deletedAt IS NULL AND e.eventStatus != 'cancelled' " +
           "AND ((e.eventStatus = 'upcoming' AND e.eventStartDate <= :today) " +
           "OR (e.eventStatus = 'active' AND e.eventEndDate < :today))")
    List<Event> findEventsNeedingStatusUpdate(@Param("today") LocalDate today);

    /**
     * Find completed or cancelled events older than specified date
     * For hiding old events in frontend
     */
    @Query("SELECT e FROM Event e WHERE e.deletedAt IS NULL " +
           "AND (e.eventStatus = 'completed' OR e.eventStatus = 'cancelled') " +
           "AND (e.eventEndDate < :cutoffDate OR e.deletedAt < :cutoffDateTime)")
    List<Event> findOldCompletedOrCancelledEvents(
        @Param("cutoffDate") LocalDate cutoffDate,
        @Param("cutoffDateTime") java.time.LocalDateTime cutoffDateTime
    );
}
