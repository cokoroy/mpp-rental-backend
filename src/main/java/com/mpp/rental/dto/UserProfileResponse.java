package com.mpp.rental.dto;

import com.mpp.rental.model.User.UserCategory;
import com.mpp.rental.model.User.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhoneNumber;
    private UserCategory userCategory;
    private UserStatus userStatus;
    private String userAddress;
    private LocalDateTime userRegisteredAt;
    private LocalDateTime userLastLogin;
    private Boolean emailVerified;

    // Bank Account Info
    private String bankName;
    private String bankAccNumber;
}