package com.mpp.rental.service;

import com.mpp.rental.dto.CreateFacilityRequest;
import com.mpp.rental.dto.FacilityResponse;
import com.mpp.rental.dto.FacilitySearchFilterRequest;
import com.mpp.rental.dto.UpdateFacilityRequest;
import com.mpp.rental.exception.DuplicateFacilityException;
import com.mpp.rental.exception.FacilityNotFoundException;
import com.mpp.rental.model.Facility;
import com.mpp.rental.repository.FacilityRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final FileStorageService fileStorageService;

    /**
     * Create new facility
     */
    @Transactional
    public FacilityResponse createFacility(CreateFacilityRequest request, MultipartFile image) throws IOException {
        // Check for duplicate facility name
        if (facilityRepository.existsByFacilityNameIgnoreCaseAndDeletedAtIsNull(request.getFacilityName())) {
            throw new DuplicateFacilityException(request.getFacilityName());
        }

        // Create facility entity
        Facility facility = new Facility();
        facility.setFacilityName(request.getFacilityName());
        facility.setFacilitySize(request.getFacilitySize());
        facility.setFacilityType(request.getFacilityType());
        facility.setFacilityDesc(request.getFacilityDesc());
        facility.setFacility_usage(request.getUsage());
        facility.setRemark(request.getRemark());
        facility.setFacilityBaseStudentPrice(request.getFacilityBaseStudentPrice());
        facility.setFacilityBaseNonstudentPrice(request.getFacilityBaseNonstudentPrice());
        facility.setFacilityStatus(request.getFacilityStatus());

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            String imagePath = fileStorageService.saveFile(image);
            facility.setFacilityImage(imagePath);
        }

        // Save facility
        Facility savedFacility = facilityRepository.save(facility);

        return mapToResponse(savedFacility);
    }

    /**
     * Get all facilities with search and filters
     */
    @Transactional(readOnly = true)
    public List<FacilityResponse> getAllFacilities(FacilitySearchFilterRequest filter) {
        Specification<Facility> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude deleted facilities (always apply)
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            // Apply search query
            if (filter.getSearchQuery() != null && !filter.getSearchQuery().trim().isEmpty()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("facilityName")),
                                "%" + filter.getSearchQuery().toLowerCase() + "%"
                        )
                );
            }

            // Apply type filter
            if (filter.getFacilityType() != null && !filter.getFacilityType().equals("all")) {
                predicates.add(criteriaBuilder.equal(root.get("facilityType"), filter.getFacilityType()));
            }

            // Apply size filter
            if (filter.getFacilitySize() != null && !filter.getFacilitySize().equals("all")) {
                predicates.add(criteriaBuilder.equal(root.get("facilitySize"), filter.getFacilitySize()));
            }

            // Apply status filter
            if (filter.getFacilityStatus() != null && !filter.getFacilityStatus().equals("all")) {
                predicates.add(criteriaBuilder.equal(root.get("facilityStatus"), filter.getFacilityStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Execute query with sorting
        Sort sort = Sort.by(Sort.Direction.DESC, "facilityCreateAt");
        List<Facility> facilities = facilityRepository.findAll(spec, sort);

        return facilities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get facility by ID
     */
    @Transactional(readOnly = true)
    public FacilityResponse getFacilityById(Integer facilityId) {
        Facility facility = facilityRepository.findByFacilityIdAndDeletedAtIsNull(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException(facilityId));
        return mapToResponse(facility);
    }

    /**
     * Update facility
     */
    @Transactional
    public FacilityResponse updateFacility(Integer facilityId, UpdateFacilityRequest request, MultipartFile image, boolean removeImage) throws IOException {
        // Find existing facility
        Facility facility = facilityRepository.findByFacilityIdAndDeletedAtIsNull(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException(facilityId));

        // Check for duplicate name (excluding current facility)
        if (facilityRepository.existsByFacilityNameAndNotId(request.getFacilityName(), facilityId)) {
            throw new DuplicateFacilityException(request.getFacilityName());
        }

        // Update fields
        facility.setFacilityName(request.getFacilityName());
        facility.setFacilitySize(request.getFacilitySize());
        facility.setFacilityType(request.getFacilityType());
        facility.setFacilityDesc(request.getFacilityDesc());
        facility.setFacility_usage(request.getUsage());
        facility.setRemark(request.getRemark());
        facility.setFacilityBaseStudentPrice(request.getFacilityBaseStudentPrice());
        facility.setFacilityBaseNonstudentPrice(request.getFacilityBaseNonstudentPrice());
        facility.setFacilityStatus(request.getFacilityStatus());

        // Handle image removal or update
        if (removeImage) {
            // Remove existing image
            if (facility.getFacilityImage() != null) {
                fileStorageService.deleteFile(facility.getFacilityImage());
                facility.setFacilityImage(null);
            }
        } else if (image != null && !image.isEmpty()) {
            // Update with new image
            String newImagePath = fileStorageService.replaceFile(facility.getFacilityImage(), image);
            facility.setFacilityImage(newImagePath);
        }
        // If neither removeImage nor new image provided, keep existing image

        // Save updated facility
        Facility updatedFacility = facilityRepository.save(facility);

        return mapToResponse(updatedFacility);
    }

    /**
     * Delete facility (soft delete)
     */
    @Transactional
    public void deleteFacility(Integer facilityId) {
        Facility facility = facilityRepository.findByFacilityIdAndDeletedAtIsNull(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException(facilityId));

        // Soft delete
        facility.setDeletedAt(LocalDateTime.now());
        facilityRepository.save(facility);

        // Optional: Delete associated image file
        if (facility.getFacilityImage() != null) {
            fileStorageService.deleteFile(facility.getFacilityImage());
        }
    }

    /**
     * Toggle facility status
     */
    @Transactional
    public FacilityResponse toggleFacilityStatus(Integer facilityId) {
        Facility facility = facilityRepository.findByFacilityIdAndDeletedAtIsNull(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException(facilityId));

        // Toggle status
        String newStatus = facility.getFacilityStatus().equals("active") ? "inactive" : "active";
        facility.setFacilityStatus(newStatus);

        Facility updatedFacility = facilityRepository.save(facility);
        return mapToResponse(updatedFacility);
    }

    /**
     * Get all active facilities only
     */
    @Transactional(readOnly = true)
    public List<FacilityResponse> getActiveFacilities() {
        List<Facility> facilities = facilityRepository.findByFacilityStatusAndDeletedAtIsNull("active");
        return facilities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Facility entity to FacilityResponse DTO
     */
    private FacilityResponse mapToResponse(Facility facility) {
        FacilityResponse response = new FacilityResponse();
        response.setFacilityId(facility.getFacilityId());
        response.setFacilityName(facility.getFacilityName());
        response.setFacilitySize(facility.getFacilitySize());
        response.setFacilityType(facility.getFacilityType());
        response.setFacilityDesc(facility.getFacilityDesc());
        response.setUsage(facility.getFacility_usage());
        response.setRemark(facility.getRemark());
        response.setFacilityImage(facility.getFacilityImage());
        response.setFacilityBaseStudentPrice(facility.getFacilityBaseStudentPrice());
        response.setFacilityBaseNonstudentPrice(facility.getFacilityBaseNonstudentPrice());
        response.setFacilityStatus(facility.getFacilityStatus());
        response.setFacilityCreateAt(facility.getFacilityCreateAt());
        return response;
    }
}