package com.mpp.rental.repository;

import com.mpp.rental.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Integer> {

    /**
     * Find all tickets submitted by a specific user (Business Owner)
     * Ordered by latest first
     */
    @Query("SELECT st FROM SupportTicket st " +
            "JOIN FETCH st.user u " +
            "WHERE st.user.userId = :userId " +
            "ORDER BY st.createdAt DESC")
    List<SupportTicket> findAllByUserId(@Param("userId") Long userId);

    /**
     * Find a single ticket with responses eagerly loaded (for BO - own ticket only)
     */
    @Query("SELECT st FROM SupportTicket st " +
            "JOIN FETCH st.user u " +
            "LEFT JOIN FETCH st.responses r " +
            "WHERE st.ticketId = :ticketId " +
            "AND st.user.userId = :userId")
    Optional<SupportTicket> findByTicketIdAndUserId(
            @Param("ticketId") Integer ticketId,
            @Param("userId") Long userId
    );

    /**
     * Find all tickets (for MPP) with filters
     */
    @Query("SELECT st FROM SupportTicket st " +
            "JOIN FETCH st.user u " +
            "WHERE (:status IS NULL OR st.ticketStatus = :status) " +
            "AND (:priority IS NULL OR st.ticketPriority = :priority) " +
            "AND (:category IS NULL OR st.ticketCategory = :category) " +
            "ORDER BY st.createdAt DESC")
    List<SupportTicket> findAllWithFilters(
            @Param("status") SupportTicket.TicketStatus status,
            @Param("priority") SupportTicket.TicketPriority priority,
            @Param("category") SupportTicket.TicketCategory category
    );

    /**
     * Find a single ticket with responses (for MPP - any ticket)
     */
    @Query("SELECT st FROM SupportTicket st " +
            "JOIN FETCH st.user u " +
            "LEFT JOIN FETCH st.responses r " +
            "WHERE st.ticketId = :ticketId")
    Optional<SupportTicket> findByIdWithResponses(@Param("ticketId") Integer ticketId);
}