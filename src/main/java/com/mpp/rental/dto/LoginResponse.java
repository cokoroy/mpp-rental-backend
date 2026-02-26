package com.mpp.rental.dto;

import com.mpp.rental.model.User.UserCategory;
import com.mpp.rental.model.User.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token; // JWT token
    private String tokenType = "Bearer";
    private Long userId;
    private String userName;
    private String userEmail;
    private UserCategory userCategory;
    private UserStatus userStatus;
}