package com.mpp.rental.service;

import com.mpp.rental.dto.*;
import com.mpp.rental.exception.*;
import com.mpp.rental.model.Business;
import com.mpp.rental.model.User;
import com.mpp.rental.repository.BusinessRepository;
import com.mpp.rental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// Resource imports
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads/ssm}")
    private String uploadDir;

    // ==================== BUSINESS OWNER OPERATIONS ====================

    /**
     * Create a new business
     */
    @Transactional
    public BusinessResponse createBusiness(CreateBusinessRequest request) {
        // Get currently authenticated user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate business name uniqueness (global)
        if (businessRepository.existsByBusinessName(request.getBusinessName())) {
            throw new DuplicateBusinessException("Business name '" + request.getBusinessName() + "' already exists");
        }

        // Validate SSM number for NON_STUDENT
        if (User.UserCategory.NON_STUDENT.equals(user.getUserCategory())) {
            if (request.getSsmNumber() == null || request.getSsmNumber().trim().isEmpty()) {
                throw new BusinessException("SSM number is required for non-student business owners");
            }

            // Check SSM uniqueness
            if (businessRepository.existsBySsmNumber(request.getSsmNumber())) {
                throw new DuplicateBusinessException("SSM number already exists");
            }
        }

        // Create business entity
        Business business = Business.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .ssmNumber(request.getSsmNumber())
                .businessCategory(request.getBusinessCategory())
                .businessDesc(request.getBusinessDesc())
                .businessStatus("ACTIVE")
                .build();

        Business savedBusiness = businessRepository.save(business);
        log.info("Business created successfully: {}", savedBusiness.getBusinessName());

        return mapToBusinessResponse(savedBusiness);
    }

    /**
     * Get all businesses owned by current user
     */
    public List<BusinessResponse> getMyBusinesses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Business> businesses = businessRepository.findByUser(user);

        return businesses.stream()
                .map(this::mapToBusinessResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get single business by ID (must be owner)
     */
    public BusinessResponse getBusinessById(Long businessId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        // Check ownership
        if (!business.getUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("You don't have permission to access this business");
        }

        return mapToBusinessResponse(business);
    }

    /**
     * Update business details
     */
    @Transactional
    public BusinessResponse updateBusiness(Long businessId, UpdateBusinessRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        // Check ownership
        if (!business.getUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("You don't have permission to update this business");
        }

        // Check if blocked
        if ("BLOCKED".equals(business.getBusinessStatus())) {
            throw new BusinessException("Cannot update blocked business");
        }

        // Validate business name uniqueness (exclude current business)
        if (!business.getBusinessName().equals(request.getBusinessName())) {
            if (businessRepository.existsByBusinessNameAndBusinessIdNot(request.getBusinessName(), businessId)) {
                throw new DuplicateBusinessException("Business name '" + request.getBusinessName() + "' already exists");
            }
        }

        // Validate SSM number for NON_STUDENT
        if (User.UserCategory.NON_STUDENT.equals(user.getUserCategory())) {
            if (request.getSsmNumber() == null || request.getSsmNumber().trim().isEmpty()) {
                throw new BusinessException("SSM number is required for non-student business owners");
            }

            // Check SSM uniqueness (exclude current business)
            if (business.getSsmNumber() == null || !business.getSsmNumber().equals(request.getSsmNumber())) {
                if (businessRepository.existsBySsmNumberAndBusinessIdNot(request.getSsmNumber(), businessId)) {
                    throw new DuplicateBusinessException("SSM number already exists");
                }
            }
        }

        // Update business fields
        business.setBusinessName(request.getBusinessName());
        business.setSsmNumber(request.getSsmNumber());
        business.setBusinessCategory(request.getBusinessCategory());
        business.setBusinessDesc(request.getBusinessDesc());

        Business updatedBusiness = businessRepository.save(business);
        log.info("Business updated successfully: {}", updatedBusiness.getBusinessName());

        return mapToBusinessResponse(updatedBusiness);
    }

    /**
     * Delete business (only if no pending/approved applications)
     */
    @Transactional
    public void deleteBusiness(Long businessId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        // Check ownership
        if (!business.getUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("You don't have permission to delete this business");
        }

        // TODO: Check if business has pending or approved applications
        // This will be implemented when we create FacilityApplication module
        // For now, allow deletion
        // Example:
        // boolean hasActiveApplications = applicationRepository.existsByBusinessAndStatusIn(
        //     business, Arrays.asList("PENDING", "APPROVED")
        // );
        // if (hasActiveApplications) {
        //     throw new BusinessException("Cannot delete business with pending or approved applications");
        // }

        businessRepository.delete(business);
        log.info("Business deleted successfully: {}", business.getBusinessName());
    }

    /**
     * Upload SSM document
     */
    @Transactional
    public BusinessResponse uploadSsmDocument(Long businessId, MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        // Check ownership
        if (!business.getUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("You don't have permission to upload document for this business");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("application/pdf") &&
                        !contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png"))) {
            throw new BusinessException("Only PDF, JPG, and PNG files are allowed");
        }

        // Validate file size (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("File size must not exceed 5MB");
        }

        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = "ssm_" + businessId + "_" + UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Delete old SSM document if exists
            if (business.getSsmDocument() != null) {
                Path oldFile = uploadPath.resolve(business.getSsmDocument());
                Files.deleteIfExists(oldFile);
            }

            // Update business entity
            business.setSsmDocument(filename);
            Business updatedBusiness = businessRepository.save(business);

            log.info("SSM document uploaded successfully for business: {}", business.getBusinessName());
            return mapToBusinessResponse(updatedBusiness);

        } catch (IOException e) {
            log.error("Failed to upload SSM document", e);
            throw new BusinessException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Download SSM document
     */
    public Resource downloadSsmDocument(Long businessId, User currentUser) throws IOException {
        // Get current user from SecurityContext (don't rely on currentUser parameter)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User authenticatedUser = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));

        // Get the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessException("Business not found"));

        // Check authorization
        if (!business.getUser().getUserId().equals(authenticatedUser.getUserId())
                && !authenticatedUser.getUserCategory().equals(User.UserCategory.MPP)) {
            throw new BusinessException("Access denied");
        }

        // Check if document exists
        if (business.getSsmDocument() == null || business.getSsmDocument().isEmpty()) {
            throw new BusinessException("No SSM document found");
        }

        // Get file path (use uploadDir property)
        Path filePath = Paths.get(uploadDir).resolve(business.getSsmDocument()).normalize();

        if (!Files.exists(filePath)) {
            throw new BusinessException("File not found on server");
        }

        // Load as Resource
        UrlResource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new BusinessException("Could not read file");
        }

        log.info("Downloaded SSM document for business: {}", business.getBusinessName());
        return resource;
    }

    // ==================== MPP OPERATIONS ====================

    /**
     * MPP: Get all businesses
     */
    public List<BusinessResponse> getAllBusinesses() {
        List<Business> businesses = businessRepository.findAll();

        return businesses.stream()
                .map(this::mapToBusinessResponse)
                .collect(Collectors.toList());
    }

    /**
     * MPP: Get all businesses by owner ID
     */
    public List<BusinessResponse> getBusinessesByOwnerId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<Business> businesses = businessRepository.findByUser(user);

        return businesses.stream()
                .map(this::mapToBusinessResponse)
                .collect(Collectors.toList());
    }

    /**
     * MPP: Get business by ID (admin view)
     */
    public BusinessResponse getBusinessByIdAdmin(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        return mapToBusinessResponse(business);
    }

    /**
     * MPP: Block/Activate business owner
     * When blocking: all businesses blocked, pending applications rejected
     */
    @Transactional
    public void updateBusinessOwnerStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<Business> businesses = businessRepository.findByUser(user);

        if ("BLOCKED".equals(status)) {
            // TODO: Check if any business has approved applications
            // This will be implemented when we create FacilityApplication module
            // Example:
            // for (Business business : businesses) {
            //     boolean hasApprovedApplications = applicationRepository
            //         .existsByBusinessAndStatus(business, "APPROVED");
            //     if (hasApprovedApplications) {
            //         throw new BusinessException(
            //             "Cannot block business owner with approved applications. " +
            //             "Business: " + business.getBusinessName()
            //         );
            //     }
            // }

            // Block all businesses
            for (Business business : businesses) {
                business.setBusinessStatus("BLOCKED");
                businessRepository.save(business);
            }

            // TODO: Reject all pending applications
            // applicationRepository.rejectPendingApplicationsByBusinessOwner(userId, "Business owner blocked by admin");

            log.info("Blocked all businesses for user: {}", user.getUserEmail());

        } else if ("ACTIVE".equals(status)) {
            // Activate all businesses
            for (Business business : businesses) {
                business.setBusinessStatus("ACTIVE");
                businessRepository.save(business);
            }

            log.info("Activated all businesses for user: {}", user.getUserEmail());
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Map Business entity to BusinessResponse DTO
     */
    private BusinessResponse mapToBusinessResponse(Business business) {
        return BusinessResponse.builder()
                .businessId(business.getBusinessId())
                .businessName(business.getBusinessName())
                .ssmNumber(business.getSsmNumber())
                .businessCategory(business.getBusinessCategory())
                .businessDesc(business.getBusinessDesc())
                .businessStatus(business.getBusinessStatus())
                .businessRegisteredAt(business.getBusinessRegisteredAt())
                .ssmDocument(business.getSsmDocument())
                .userId(business.getUser().getUserId())
                .ownerName(business.getUser().getUserName())
                .ownerEmail(business.getUser().getUserEmail())
                .ownerCategory(business.getUser().getUserCategory().name())
                .ownerPhoneNumber(business.getUser().getUserPhoneNumber())
                .build();
    }
}