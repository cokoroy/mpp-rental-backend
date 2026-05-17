package com.mpp.rental.service;

import com.mpp.rental.dto.NotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseEmitterService {

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SseEmitter createEmitter(Long userId) {
        // Close ALL existing connections for this user first.
        // Prevents duplicate notifications when EventSource reconnects
        // and the old emitter is still registered alongside the new one.
        List<SseEmitter> existing = emitters.remove(userId);
        if (existing != null) {
            for (SseEmitter old : existing) {
                try { old.complete(); } catch (Exception ignored) {}
            }
            log.info("Closed {} stale SSE connection(s) for userId={}", existing.size(), userId);
        }

        // Long timeout — 30 minutes. Frontend EventSource will reconnect if it expires.
        SseEmitter emitter = new SseEmitter(1_800_000L);

        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        log.info("SSE emitter created for userId={}, total connections={}", userId,
                emitters.get(userId).size());

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        // Send heartbeat immediately to confirm connection
        try {
            emitter.send(SseEmitter.event().name("connected").data("SSE connection established"));
        } catch (IOException e) {
            log.warn("Failed to send initial heartbeat to userId={}", userId);
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    public void sendToUser(Long userId, NotificationDTO notification) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            log.debug("No SSE connections for userId={}", userId);
            return;
        }

        String payload = toJson(notification);
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(payload));
                log.debug("SSE notification sent to userId={}", userId);
            } catch (IOException e) {
                log.warn("Dead SSE emitter for userId={}, removing", userId);
                deadEmitters.add(emitter);
            }
        }

        userEmitters.removeAll(deadEmitters);
    }

    public boolean isUserOnline(Long userId) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        return userEmitters != null && !userEmitters.isEmpty();
    }

    /**
     * Heartbeat every 25 seconds — keeps the connection alive through
     * proxies and load balancers that close idle connections.
     * Also flushes dead emitters proactively.
     */
    @Scheduled(fixedDelay = 25000)
    public void sendHeartbeat() {
        emitters.forEach((userId, userEmitters) -> {
            List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            }
            if (!deadEmitters.isEmpty()) {
                userEmitters.removeAll(deadEmitters);
                log.debug("Removed {} dead emitter(s) for userId={} during heartbeat", deadEmitters.size(), userId);
            }
        });
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
                log.info("All SSE connections closed for userId={}", userId);
            }
        }
    }

    private String toJson(NotificationDTO n) {
        return "{" +
                "\"notificationId\":" + n.getNotificationId() + "," +
                "\"type\":\"" + escapeJson(n.getType()) + "\"," +
                "\"title\":\"" + escapeJson(n.getTitle()) + "\"," +
                "\"message\":\"" + escapeJson(n.getMessage()) + "\"," +
                "\"referenceId\":" + (n.getReferenceId() != null ? n.getReferenceId() : "null") + "," +
                "\"referenceType\":" + (n.getReferenceType() != null ? "\"" + escapeJson(n.getReferenceType()) + "\"" : "null") + "," +
                "\"read\":" + n.isRead() + "," +
                "\"createdAt\":\"" + (n.getCreatedAt() != null ? n.getCreatedAt().format(FORMATTER) : "") + "\"" +
                "}";
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}