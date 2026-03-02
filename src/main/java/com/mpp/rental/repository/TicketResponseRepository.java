package com.mpp.rental.repository;

import com.mpp.rental.model.TicketResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketResponseRepository extends JpaRepository<TicketResponse, Integer> {

    /**
     * Find all responses for a ticket ordered by creation time (oldest first for chat thread)
     */
    @Query("SELECT tr FROM TicketResponse tr " +
            "JOIN FETCH tr.sender s " +
            "WHERE tr.supportTicket.ticketId = :ticketId " +
            "ORDER BY tr.createdAt ASC")
    List<TicketResponse> findAllByTicketId(@Param("ticketId") Integer ticketId);
}