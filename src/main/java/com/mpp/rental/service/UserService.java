package com.mpp.rental.service;

import com.mpp.rental.dto.*;
import com.mpp.rental.exception.BadRequestException;
import com.mpp.rental.exception.ResourceNotFoundException;
import com.mpp.rental.model.BankAccount;
import com.mpp.rental.model.Business;
import com.mpp.rental.model.User;
import com.mpp.rental.model.User.UserStatus;
import com.mpp.rental.model.User.UserCategory;
import com.mpp.rental.repository.BankAccountRepository;
import com.mpp.rental.repository.UserRepository;
import com.mpp.rental.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UserService - Contains business logic for user management
 */
@Service
@RequiredArgsConstructor
@Transactional // All methods run in database transactions
public class UserService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    // ==================== EXISTING METHODS ====================

    /**
     * Register new user
     */
    public UserProfileResponse registerUser(RegisterRequest request) {
        // 1. Validate passwords match
        if (!request.getUserPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // 2. Check if email already exists
        if (userRepository.existsByUserEmail(request.getUserEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // 3. Check if bank account number already exists
        if (bankAccountRepository.existsByBankAccNumber(request.getBankAccNumber())) {
            throw new BadRequestException("Bank account number already registered");
        }

        // 4. Create User entity
        User user = new User();
        user.setUserName(request.getUserName());
        user.setUserEmail(request.getUserEmail());
        user.setUserPhoneNumber(request.getUserPhoneNumber());
        user.setUserPassword(passwordEncoder.encode(request.getUserPassword())); // Hash password
        user.setUserCategory(request.getUserCategory());
        user.setUserAddress(request.getUserAddress());

        // Set initial status based on user category
        if (request.getUserCategory() == UserCategory.MPP) {
            user.setUserStatus(UserStatus.PENDING); // MPP needs approval
        } else {
            user.setUserStatus(UserStatus.ACTIVE); // Business owners are auto-approved
        }

        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString()); // Generate verification token
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24)); // Token expires in 24 hours

        // 5. Create BankAccount entity
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankName(request.getBankName());
        bankAccount.setBankAccNumber(request.getBankAccNumber());
        bankAccount.setUser(user); // Link to user

        // Set bidirectional relationship
        user.setBankAccount(bankAccount);

        // 6. Save to database (cascade saves bank account too)
        User savedUser = userRepository.save(user);

        // 7. TODO: Send verification email (implement later)
        // emailService.sendVerificationEmail(savedUser.getUserEmail(), savedUser.getVerificationToken());

        // 8. Return response DTO
        return mapToUserProfileResponse(savedUser);
    }

    /**
     * Login user
     */
    public LoginResponse loginUser(LoginRequest request) {
        // 1. Authenticate user
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUserEmail(),
                            request.getUserPassword()
                    )
            );
        } catch (Exception e) {
            throw new BadRequestException("Invalid email or password");
        }

        // 2. Load user details
        User user = userRepository.findByUserEmail(request.getUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Check if user is active
        if (user.getUserStatus() == UserStatus.BLOCKED) {
            throw new BadRequestException("Your account has been blocked. Please contact administrator.");
        }

        if (user.getUserStatus() == UserStatus.PENDING) {
            throw new BadRequestException("Your account is pending approval. Please wait for administrator approval.");
        }

        // 4. Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUserEmail());
        String token = jwtUtil.generateToken(userDetails, request.isRememberMe());

        // 5. Update last login time
        user.setUserLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 6. Return login response
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userCategory(user.getUserCategory())
                .userStatus(user.getUserStatus())
                .build();
    }

    /**
     * Get user profile by ID
     */
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return mapToUserProfileResponse(user);
    }

    /**
     * Get user profile by email
     */
    public UserProfileResponse getUserProfileByEmail(String email) {
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return mapToUserProfileResponse(user);
    }

    /**
     * Update user profile (by user themselves)
     */
    public UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Update user fields (only if provided)
        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            user.setUserName(request.getUserName());
        }

        if (request.getUserPhoneNumber() != null && !request.getUserPhoneNumber().isBlank()) {
            user.setUserPhoneNumber(request.getUserPhoneNumber());
        }

        if (request.getUserAddress() != null && !request.getUserAddress().isBlank()) {
            user.setUserAddress(request.getUserAddress());
        }

        // Update bank account (if provided)
        if (request.getBankName() != null || request.getBankAccNumber() != null) {
            BankAccount bankAccount = user.getBankAccount();

            if (bankAccount == null) {
                bankAccount = new BankAccount();
                bankAccount.setUser(user);
                user.setBankAccount(bankAccount);
            }

            if (request.getBankName() != null && !request.getBankName().isBlank()) {
                bankAccount.setBankName(request.getBankName());
            }

            if (request.getBankAccNumber() != null && !request.getBankAccNumber().isBlank()) {
                // Check if new bank account number already exists
                if (!bankAccount.getBankAccNumber().equals(request.getBankAccNumber()) &&
                        bankAccountRepository.existsByBankAccNumber(request.getBankAccNumber())) {
                    throw new BadRequestException("Bank account number already exists");
                }
                bankAccount.setBankAccNumber(request.getBankAccNumber());
            }
        }

        User updatedUser = userRepository.save(user);
        return mapToUserProfileResponse(updatedUser);
    }

    /**
     * Change user password
     */
    public void changePassword(String email, ChangePasswordRequest request) {
        // Find user
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getUserPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Verify new passwords match
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("New passwords do not match");
        }

        // Verify new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), user.getUserPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        // Update password
        user.setUserPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ==================== NEW METHODS FOR MPP USER MANAGEMENT ====================

    /**
     * Get all users with their businesses (for MPP User Management)
     * @return List of users with business summaries
     */
    public List<UserManagementResponse> getAllUsers() {
        List<User> users = userRepository.findAllWithBusinessesAndBankAccount();

        return users.stream()
                .map(this::mapToUserManagementResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search and filter users (for MPP User Management)
     * @param searchQuery Search by name, email, or phone
     * @param category Filter by user category
     * @param status Filter by user status
     * @return Filtered list of users
     */
    public List<UserManagementResponse> searchUsers(
            String searchQuery,
            UserCategory category,
            UserStatus status) {

        List<User> users = userRepository.searchUsers(searchQuery, category, status);

        return users.stream()
                .map(this::mapToUserManagementResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get complete user details including password (MPP only)
     * @param userId User ID
     * @return Complete user details with password
     */
    public UserDetailsResponse getUserDetailsById(Long userId) {
        User user = userRepository.findByIdWithBusinessesAndBankAccount(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return mapToUserDetailsResponse(user);
    }

    /**
     * Update user by MPP (can update all fields including password and role)
     * @param userId User ID to update
     * @param request Update request with new values
     * @return Updated user details
     */
    public UserDetailsResponse updateUserByMPP(Long userId, UpdateUserByMPPRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Check if email is being changed and if new email already exists
        if (!user.getUserEmail().equals(request.getUserEmail()) &&
                userRepository.existsByUserEmail(request.getUserEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Update user fields
        user.setUserName(request.getUserName());
        user.setUserEmail(request.getUserEmail());
        user.setUserPhoneNumber(request.getUserPhoneNumber());
        user.setUserAddress(request.getUserAddress());
        user.setUserCategory(request.getUserCategory());

        // Update password if provided
        if (request.getUserPassword() != null && !request.getUserPassword().isBlank()) {
            user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
        }

        // Update bank account
        BankAccount bankAccount = user.getBankAccount();
        if (bankAccount == null) {
            bankAccount = new BankAccount();
            bankAccount.setUser(user);
            user.setBankAccount(bankAccount);
        }

        // Check if bank account number is being changed
        if (!bankAccount.getBankAccNumber().equals(request.getBankAccNumber()) &&
                bankAccountRepository.existsByBankAccNumber(request.getBankAccNumber())) {
            throw new BadRequestException("Bank account number already registered");
        }

        bankAccount.setBankName(request.getBankName());
        bankAccount.setBankAccNumber(request.getBankAccNumber());

        // Save and return
        User updatedUser = userRepository.save(user);

        // TODO: Send notification to user about profile update

        return mapToUserDetailsResponse(updatedUser);
    }

    /**
     * Toggle user status (Block/Activate)
     * @param userId User ID
     * @return Updated user details
     */
    public UserDetailsResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Toggle status
        if (user.getUserStatus() == UserStatus.ACTIVE) {
            user.setUserStatus(UserStatus.BLOCKED);
        } else {
            user.setUserStatus(UserStatus.ACTIVE);
        }

        User updatedUser = userRepository.save(user);

        // TODO: Send notification to user about status change

        return mapToUserDetailsResponse(updatedUser);
    }

    // ==================== HELPER MAPPING METHODS ====================

    /**
     * Map User entity to UserProfileResponse DTO
     */
    private UserProfileResponse mapToUserProfileResponse(User user) {
        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userPhoneNumber(user.getUserPhoneNumber())
                .userCategory(user.getUserCategory())
                .userStatus(user.getUserStatus())
                .userAddress(user.getUserAddress())
                .userRegisteredAt(user.getUserRegisteredAt())
                .userLastLogin(user.getUserLastLogin())
                .emailVerified(user.getEmailVerified());

        // Add bank account info if exists
        if (user.getBankAccount() != null) {
            builder.bankName(user.getBankAccount().getBankName())
                    .bankAccNumber(user.getBankAccount().getBankAccNumber());
        }

        return builder.build();
    }

    /**
     * Map User entity to UserManagementResponse DTO
     */
    private UserManagementResponse mapToUserManagementResponse(User user) {
        // Map businesses
        List<UserManagementResponse.UserBusinessSummary> businesses = user.getBusinesses().stream()
                .map(business -> UserManagementResponse.UserBusinessSummary.builder()
                        .businessId(business.getBusinessId())
                        .businessName(business.getBusinessName())
                        .businessCategory(business.getBusinessCategory())
                        .businessStatus(business.getBusinessStatus())
                        .build())
                .collect(Collectors.toList());

        // Build response
        UserManagementResponse.UserManagementResponseBuilder builder = UserManagementResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userPhoneNumber(user.getUserPhoneNumber())
                .userCategory(user.getUserCategory())
                .userStatus(user.getUserStatus())
                .userAddress(user.getUserAddress())
                .userRegisteredAt(user.getUserRegisteredAt())
                .userLastLogin(user.getUserLastLogin())
                .businesses(businesses);

        // Add bank account info if exists
        if (user.getBankAccount() != null) {
            builder.bankName(user.getBankAccount().getBankName())
                    .bankAccNumber(user.getBankAccount().getBankAccNumber());
        }

        return builder.build();
    }

    /**
     * Map User entity to UserDetailsResponse DTO (includes password)
     */
    private UserDetailsResponse mapToUserDetailsResponse(User user) {
        // Map businesses with more details
        List<UserDetailsResponse.BusinessDetails> businesses = user.getBusinesses().stream()
                .map(business -> UserDetailsResponse.BusinessDetails.builder()
                        .businessId(business.getBusinessId())
                        .businessName(business.getBusinessName())
                        .businessCategory(business.getBusinessCategory())
                        .businessStatus(business.getBusinessStatus())
                        .ssmNumber(business.getSsmNumber())
                        .businessRegisteredAt(business.getBusinessRegisteredAt())
                        .build())
                .collect(Collectors.toList());

        // Build response
        UserDetailsResponse.UserDetailsResponseBuilder builder = UserDetailsResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userPhoneNumber(user.getUserPhoneNumber())
                .userAddress(user.getUserAddress())
                .userPassword(user.getUserPassword()) // Include encrypted password (will be shown as-is to MPP)
                .userCategory(user.getUserCategory())
                .userStatus(user.getUserStatus())
                .userRegisteredAt(user.getUserRegisteredAt())
                .userLastLogin(user.getUserLastLogin())
                .emailVerified(user.getEmailVerified())
                .businesses(businesses);

        // Add bank account info if exists
        if (user.getBankAccount() != null) {
            builder.bankName(user.getBankAccount().getBankName())
                    .bankAccNumber(user.getBankAccount().getBankAccNumber());
        }

        return builder.build();
    }
}