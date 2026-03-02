package com.mpp.rental.service;

import com.mpp.rental.dto.*;
import com.mpp.rental.exception.BadRequestException;
import com.mpp.rental.exception.ResourceNotFoundException;
import com.mpp.rental.exception.SupportTicketException;
import com.mpp.rental.model.SupportTicket;
import com.mpp.rental.model.TicketResponse;
import com.mpp.rental.model.User;
import com.mpp.rental.repository.SupportTicketRepository;
import com.mpp.rental.repository.TicketResponseRepository;
import com.mpp.rental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final TicketResponseRepository ticketResponseRepository;
    private final UserRepository userRepository;

    // ==================== HELPER ====================

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ==================== BUSINESS OWNER - TICKET CRUD ====================

    /**
     * Create a new support ticket (Business Owner)
     */
    @Transactional
    public SupportTicketResponse createTicket(CreateTicketRequest request) {
        User user = getCurrentUser();

        // Parse and validate category
        SupportTicket.TicketCategory category;
        try {
            category = SupportTicket.TicketCategory.valueOf(request.getTicketCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid ticket category: " + request.getTicketCategory());
        }

        // Parse and validate priority
        SupportTicket.TicketPriority priority;
        try {
            priority = SupportTicket.TicketPriority.valueOf(request.getTicketPriority().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid ticket priority: " + request.getTicketPriority());
        }

        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setTicketTitle(request.getTicketTitle());
        ticket.setTicketDescription(request.getTicketDescription());
        ticket.setTicketCategory(category);
        ticket.setTicketPriority(priority);
        ticket.setTicketStatus(SupportTicket.TicketStatus.OPEN);

        SupportTicket saved = supportTicketRepository.save(ticket);
        log.info("Support ticket created: ticketId={} by userId={}", saved.getTicketId(), user.getUserId());

        return mapToResponse(saved, null);
    }

    /**
     * Get all tickets submitted by the current Business Owner (list view - no responses)
     */
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getMyTickets() {
        User user = getCurrentUser();
        List<SupportTicket> tickets = supportTicketRepository.findAllByUserId(user.getUserId());
        return tickets.stream()
                .map(ticket -> mapToResponse(ticket, null))
                .collect(Collectors.toList());
    }

    /**
     * Get a single ticket with full conversation thread (Business Owner - own ticket only)
     */
    @Transactional(readOnly = true)
    public SupportTicketResponse getMyTicketById(Integer ticketId) {
        User user = getCurrentUser();

        SupportTicket ticket = supportTicketRepository.findByTicketIdAndUserId(ticketId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        List<TicketResponse> responses = ticketResponseRepository.findAllByTicketId(ticketId);
        return mapToResponse(ticket, responses);
    }

    /**
     * Business Owner replies to an existing ticket
     * Only allowed if ticket is OPEN or IN_PROGRESS
     */
    @Transactional
    public SupportTicketResponse replyToTicket(Integer ticketId, CreateTicketResponseRequest request) {
        User user = getCurrentUser();

        SupportTicket ticket = supportTicketRepository.findByTicketIdAndUserId(ticketId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        // Cannot reply to a RESOLVED ticket
        if (ticket.getTicketStatus() == SupportTicket.TicketStatus.RESOLVED) {
            throw new SupportTicketException("Cannot reply to a resolved ticket");
        }

        TicketResponse response = new TicketResponse();
        response.setSupportTicket(ticket);
        response.setSender(user);
        response.setMessage(request.getMessage());

        ticketResponseRepository.save(response);
        log.info("BO replied to ticketId={} by userId={}", ticketId, user.getUserId());

        List<TicketResponse> responses = ticketResponseRepository.findAllByTicketId(ticketId);
        return mapToResponse(ticket, responses);
    }

    /**
     * Delete a ticket (Business Owner - only allowed if status is OPEN)
     */
    @Transactional
    public void deleteTicket(Integer ticketId) {
        User user = getCurrentUser();

        SupportTicket ticket = supportTicketRepository.findByTicketIdAndUserId(ticketId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        // Only OPEN tickets can be deleted (once MPP replies, status becomes IN_PROGRESS)
        if (ticket.getTicketStatus() != SupportTicket.TicketStatus.OPEN) {
            throw new SupportTicketException("Ticket can only be deleted when status is OPEN");
        }

        supportTicketRepository.delete(ticket);
        log.info("Ticket deleted: ticketId={} by userId={}", ticketId, user.getUserId());
    }

    // ==================== MPP - TICKET MANAGEMENT ====================

    /**
     * Get all tickets with optional filters (MPP)
     */
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getAllTickets(String status, String priority, String category) {
        SupportTicket.TicketStatus ticketStatus = parseStatus(status);
        SupportTicket.TicketPriority ticketPriority = parsePriority(priority);
        SupportTicket.TicketCategory ticketCategory = parseCategory(category);

        List<SupportTicket> tickets = supportTicketRepository.findAllWithFilters(
                ticketStatus, ticketPriority, ticketCategory);

        return tickets.stream()
                .map(ticket -> mapToResponse(ticket, null))
                .collect(Collectors.toList());
    }

    /**
     * Get a single ticket with full conversation thread (MPP - any ticket)
     */
    @Transactional(readOnly = true)
    public SupportTicketResponse getTicketById(Integer ticketId) {
        SupportTicket ticket = supportTicketRepository.findByIdWithResponses(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        List<TicketResponse> responses = ticketResponseRepository.findAllByTicketId(ticketId);
        return mapToResponse(ticket, responses);
    }

    /**
     * MPP replies to a ticket — automatically moves status from OPEN to IN_PROGRESS
     */
    @Transactional
    public SupportTicketResponse mppReplyToTicket(Integer ticketId, CreateTicketResponseRequest request) {
        User mpp = getCurrentUser();

        SupportTicket ticket = supportTicketRepository.findByIdWithResponses(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        // Cannot reply to RESOLVED ticket
        if (ticket.getTicketStatus() == SupportTicket.TicketStatus.RESOLVED) {
            throw new SupportTicketException("Cannot reply to a resolved ticket");
        }

        // Auto-transition: OPEN → IN_PROGRESS when MPP replies
        if (ticket.getTicketStatus() == SupportTicket.TicketStatus.OPEN) {
            ticket.setTicketStatus(SupportTicket.TicketStatus.IN_PROGRESS);
            supportTicketRepository.save(ticket);
        }

        TicketResponse response = new TicketResponse();
        response.setSupportTicket(ticket);
        response.setSender(mpp);
        response.setMessage(request.getMessage());

        ticketResponseRepository.save(response);
        log.info("MPP replied to ticketId={} by userId={}", ticketId, mpp.getUserId());

        List<TicketResponse> responses = ticketResponseRepository.findAllByTicketId(ticketId);
        return mapToResponse(ticket, responses);
    }

    /**
     * MPP marks ticket as RESOLVED
     */
    @Transactional
    public SupportTicketResponse resolveTicket(Integer ticketId) {
        SupportTicket ticket = supportTicketRepository.findByIdWithResponses(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        if (ticket.getTicketStatus() == SupportTicket.TicketStatus.RESOLVED) {
            throw new SupportTicketException("Ticket is already resolved");
        }

        ticket.setTicketStatus(SupportTicket.TicketStatus.RESOLVED);
        supportTicketRepository.save(ticket);
        log.info("Ticket resolved: ticketId={}", ticketId);

        List<TicketResponse> responses = ticketResponseRepository.findAllByTicketId(ticketId);
        return mapToResponse(ticket, responses);
    }

    /**
     * MPP reopens a RESOLVED ticket → back to IN_PROGRESS
     */
    @Transactional
    public SupportTicketResponse reopenTicket(Integer ticketId) {
        SupportTicket ticket = supportTicketRepository.findByIdWithResponses(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        if (ticket.getTicketStatus() != SupportTicket.TicketStatus.RESOLVED) {
            throw new SupportTicketException("Only resolved tickets can be reopened");
        }

        ticket.setTicketStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        supportTicketRepository.save(ticket);
        log.info("Ticket reopened: ticketId={}", ticketId);

        List<TicketResponse> responses = ticketResponseRepository.findAllByTicketId(ticketId);
        return mapToResponse(ticket, responses);
    }

    /**
     * MPP updates ticket priority
     */
    @Transactional
    public SupportTicketResponse updateTicketPriority(Integer ticketId, UpdateTicketPriorityRequest request) {
        SupportTicket ticket = supportTicketRepository.findByIdWithResponses(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        SupportTicket.TicketPriority priority;
        try {
            priority = SupportTicket.TicketPriority.valueOf(request.getTicketPriority().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid ticket priority: " + request.getTicketPriority());
        }

        ticket.setTicketPriority(priority);
        supportTicketRepository.save(ticket);
        log.info("Ticket priority updated: ticketId={} priority={}", ticketId, priority);

        List<TicketResponse> responses = ticketResponseRepository.findAllByTicketId(ticketId);
        return mapToResponse(ticket, responses);
    }

    // ==================== HELPER MAPPERS ====================

    private SupportTicketResponse mapToResponse(SupportTicket ticket, List<TicketResponse> responses) {
        SupportTicketResponse response = new SupportTicketResponse();
        response.setTicketId(ticket.getTicketId());
        response.setUserId(ticket.getUser().getUserId());
        response.setUserName(ticket.getUser().getUserName());
        response.setUserEmail(ticket.getUser().getUserEmail());
        response.setUserCategory(ticket.getUser().getUserCategory().name());
        response.setTicketTitle(ticket.getTicketTitle());
        response.setTicketDescription(ticket.getTicketDescription());
        response.setTicketCategory(ticket.getTicketCategory().name());
        response.setTicketPriority(ticket.getTicketPriority().name());
        response.setTicketStatus(ticket.getTicketStatus().name());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());

        if (responses != null) {
            response.setResponses(responses.stream()
                    .map(this::mapToTicketResponseDto)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private TicketResponseDto mapToTicketResponseDto(TicketResponse tr) {
        TicketResponseDto dto = new TicketResponseDto();
        dto.setResponseId(tr.getResponseId());
        dto.setSenderId(tr.getSender().getUserId());
        dto.setSenderName(tr.getSender().getUserName());
        dto.setSenderCategory(tr.getSender().getUserCategory().name());
        dto.setMessage(tr.getMessage());
        dto.setCreatedAt(tr.getCreatedAt());
        return dto;
    }

    // ==================== ENUM PARSERS ====================

    private SupportTicket.TicketStatus parseStatus(String status) {
        if (status == null || status.equalsIgnoreCase("all")) return null;
        try {
            return SupportTicket.TicketStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status filter: " + status);
        }
    }

    private SupportTicket.TicketPriority parsePriority(String priority) {
        if (priority == null || priority.equalsIgnoreCase("all")) return null;
        try {
            return SupportTicket.TicketPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid priority filter: " + priority);
        }
    }

    private SupportTicket.TicketCategory parseCategory(String category) {
        if (category == null || category.equalsIgnoreCase("all")) return null;
        try {
            return SupportTicket.TicketCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid category filter: " + category);
        }
    }
}