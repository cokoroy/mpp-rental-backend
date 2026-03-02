package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDto {

    private Integer responseId;
    private Long senderId;
    private String senderName;
    private String senderCategory; // MPP, STUDENT, NON_STUDENT
    private String message;
    private LocalDateTime createdAt;
}