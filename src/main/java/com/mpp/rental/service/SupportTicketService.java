package com.mpp.rental.service;

import com.mpp.rental.dto.*;
import java.time.LocalDateTime;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService; // ← ADDED

    // ==================== HELPER ====================

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Broadcast ticket update to all WebSocket subscribers of this ticket
     * Topic: /topic/ticket/{ticketId}
     */
    private void broadcastTicketUpdate(Integer ticketId, String eventType, SupportTicketResponse ticket) {
        String destination = "/topic/ticket/" + ticketId;
        TicketUpdateEvent event = new TicketUpdateEvent(eventType, ticket);
        messagingTemplate.convertAndSend(destination, event);
        log.info("WebSocket broadcast: eventType={} ticketId={}", eventType, ticketId);
    }

    // ==================== BUSINESS OWNER - TICKET CRUD ====================

    /**
     * Create a new support ticket (Business Owner)
     */
    @Transactional
    public SupportTicketResponse createTicket(CreateTicketRequest request) {
        User user = getCurrentUser();

        SupportTicket.TicketCategory category;
        try {
            category = SupportTicket.TicketCategory.valueOf(request.getTicketCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid ticket category: " + request.getTicketCategory());
        }

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

        // ── NOTIFICATION: Notify MPP of new support ticket ──────────────────
        try {
            notificationService.notifySupportTicketCreated(
                    saved.getTicketId().longValue(),
                    user.getUserName(),
                    saved.getTicketTitle()
            );
        } catch (Exception e) {
            log.warn("Failed to send support ticket notification: {}", e.getMessage());
        }
        // ────────────────────────────────────────────────────────────────────

        return mapToResponse(saved, null);
    }

    /**
     * Get all tickets submitted by the current Business Owner (list view)
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
     * Broadcasts NEW_REPLY event over WebSocket
     */
    @Transactional
    public SupportTicketResponse replyToTicket(Integer ticketId, CreateTicketResponseRequest request) {
        User user = getCurrentUser();

        SupportTicket ticket = supportTicketRepository.findByTicketIdAndUserId(ticketId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

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
        SupportTicketResponse updatedTicket = mapToResponse(ticket, responses);

        // Broadcast to MPP and BO both viewing this ticket
        broadcastTicketUpdate(ticketId, "NEW_REPLY", updatedTicket);

        // ── NOTIFICATION: Notify MPP that BO replied ─────────────────────────
        try {
            notificationService.notifySupportTicketBoReplied(
                    ticketId.longValue(),
                    user.getUserName(),
                    ticket.getTicketTitle()
            );
        } catch (Exception e) {
            log.warn("Failed to send BO reply notification: {}", e.getMessage());
        }
        // ────────────────────────────────────────────────────────────────────

        return updatedTicket;
    }

    /**
     * Delete a ticket (Business Owner - only allowed if status is OPEN)
     */
    @Transactional
    public void deleteTicket(Integer ticketId) {
        User user = getCurrentUser();

        SupportTicket ticket = supportTicketRepository.findByTicketIdAndUserId(ticketId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

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
     * MPP replies to a ticket
     * Auto-transitions OPEN → IN_PROGRESS
     * Broadcasts NEW_REPLY event over WebSocket
     */
    @Transactional
    public SupportTicketResponse mppReplyToTicket(Integer ticketId, CreateTicketResponseRequest request) {
        User mpp = getCurrentUser();

        SupportTicket ticket = supportTicketRepository.findByIdWithResponses(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

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
        SupportTicketResponse updatedTicket = mapToResponse(ticket, responses);

        // Broadcast to BO and MPP both viewing this ticket
        broadcastTicketUpdate(ticketId, "NEW_REPLY", updatedTicket);

        // ── NOTIFICATION: Notify BO that MPP replied ─────────────────────────
        try {
            Long boUserId = ticket.getUser().getUserId();
            notificationService.notifySupportTicketReplied(
                    ticketId.longValue(),
                    boUserId,
                    ticket.getTicketTitle()
            );
        } catch (Exception e) {
            log.warn("Failed to send MPP reply notification to BO: {}", e.getMessage());
        }
        // ────────────────────────────────────────────────────────────────────

        return updatedTicket;
    }

    /**
     * MPP marks ticket as RESOLVED
     * Broadcasts STATUS_CHANGE event over WebSocket
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
        SupportTicketResponse updatedTicket = mapToResponse(ticket, responses);

        broadcastTicketUpdate(ticketId, "STATUS_CHANGE", updatedTicket);

        return updatedTicket;
    }

    /**
     * MPP reopens a RESOLVED ticket → back to IN_PROGRESS
     * Broadcasts STATUS_CHANGE event over WebSocket
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
        SupportTicketResponse updatedTicket = mapToResponse(ticket, responses);

        broadcastTicketUpdate(ticketId, "STATUS_CHANGE", updatedTicket);

        return updatedTicket;
    }

    /**
     * MPP updates ticket priority
     * Broadcasts PRIORITY_CHANGE event over WebSocket
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
        SupportTicketResponse updatedTicket = mapToResponse(ticket, responses);

        broadcastTicketUpdate(ticketId, "PRIORITY_CHANGE", updatedTicket);

        return updatedTicket;
    }


    // ==================== FEEDBACK ====================

    /**
     * Business Owner submits feedback on a RESOLVED ticket
     * Only allowed once — cannot resubmit if feedbackRating already set
     */
    @Transactional
    public SupportTicketResponse submitFeedback(Integer ticketId, SubmitFeedbackRequest request) {
        User user = getCurrentUser();

        SupportTicket ticket = supportTicketRepository.findByTicketIdAndUserId(ticketId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        // Only RESOLVED tickets can receive feedback
        if (ticket.getTicketStatus() != SupportTicket.TicketStatus.RESOLVED) {
            throw new SupportTicketException("Feedback can only be submitted on resolved tickets");
        }

        // Only allow feedback once
        if (ticket.getFeedbackRating() != null) {
            throw new SupportTicketException("Feedback has already been submitted for this ticket");
        }

        ticket.setFeedbackRating(request.getFeedbackRating());
        ticket.setFeedbackComment(request.getFeedbackComment());
        ticket.setFeedbackSubmittedAt(LocalDateTime.now());
        supportTicketRepository.save(ticket);

        log.info("Feedback submitted: ticketId={} rating={}", ticketId, request.getFeedbackRating());

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
        response.setFeedbackRating(ticket.getFeedbackRating());
        response.setFeedbackComment(ticket.getFeedbackComment());
        response.setFeedbackSubmittedAt(ticket.getFeedbackSubmittedAt());
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