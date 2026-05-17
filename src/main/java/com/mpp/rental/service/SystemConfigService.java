package com.mpp.rental.service;

import com.mpp.rental.dto.SystemConfigRequest;
import com.mpp.rental.dto.SystemConfigResponse;
import com.mpp.rental.exception.BadRequestException;
import com.mpp.rental.exception.ResourceNotFoundException;
import com.mpp.rental.model.SystemConfig;
import com.mpp.rental.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

    private final SystemConfigRepository configRepository;

    // Valid groups
    private static final List<String> VALID_GROUPS = List.of(
            SystemConfig.GROUP_BUSINESS_CATEGORY,
            SystemConfig.GROUP_FACILITY_TYPE,
            SystemConfig.GROUP_FACILITY_SIZE,
            SystemConfig.GROUP_FACILITY_USAGE,
            SystemConfig.GROUP_EVENT_TYPE
    );

    // ==================== PUBLIC ENDPOINTS (all authenticated users) ====================

    /**
     * Get all active items for a config group — used by MPP/BO dropdowns
     */
    @Transactional(readOnly = true)
    public List<String> getActiveValues(String configGroup) {
        return configRepository
                .findByConfigGroupAndIsActiveTrueOrderByDisplayOrderAsc(configGroup)
                .stream()
                .map(SystemConfig::getConfigValue)
                .collect(Collectors.toList());
    }

    // ==================== SUPER ADMIN MANAGEMENT ====================

    /**
     * Get all items in a group (including inactive) for Super Admin
     */
    @Transactional(readOnly = true)
    public List<SystemConfigResponse> getAllByGroup(String configGroup) {
        validateGroup(configGroup);
        return configRepository.findByConfigGroupOrderByDisplayOrderAsc(configGroup)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Get all configs across all groups for Super Admin overview
     */
    @Transactional(readOnly = true)
    public List<SystemConfigResponse> getAll() {
        return configRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Add new config item
     */
    @Transactional
    public SystemConfigResponse create(SystemConfigRequest req) {
        validateGroup(req.getConfigGroup());
        if (configRepository.existsByConfigGroupAndConfigValue(req.getConfigGroup(), req.getConfigValue())) {
            throw new BadRequestException("'" + req.getConfigValue() + "' already exists in this group.");
        }
        SystemConfig config = new SystemConfig();
        config.setConfigGroup(req.getConfigGroup());
        config.setConfigValue(req.getConfigValue());
        config.setDisplayOrder(req.getDisplayOrder());
        config.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        return toResponse(configRepository.save(config));
    }

    /**
     * Update config item
     */
    @Transactional
    public SystemConfigResponse update(Integer configId, SystemConfigRequest req) {
        SystemConfig config = configRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Config item not found: " + configId));
        validateGroup(req.getConfigGroup());

        // Check duplicate value (excluding self)
        if (configRepository.existsByConfigGroupAndConfigValueAndConfigIdNot(
                req.getConfigGroup(), req.getConfigValue(), configId)) {
            throw new BadRequestException("'" + req.getConfigValue() + "' already exists in this group.");
        }
        config.setConfigValue(req.getConfigValue());
        config.setDisplayOrder(req.getDisplayOrder());
        config.setIsActive(req.getIsActive() != null ? req.getIsActive() : config.getIsActive());
        return toResponse(configRepository.save(config));
    }

    /**
     * Toggle active status
     */
    @Transactional
    public SystemConfigResponse toggleActive(Integer configId) {
        SystemConfig config = configRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Config item not found: " + configId));
        config.setIsActive(!config.getIsActive());
        return toResponse(configRepository.save(config));
    }

    /**
     * Delete config item permanently
     */
    @Transactional
    public void delete(Integer configId) {
        if (!configRepository.existsById(configId)) {
            throw new ResourceNotFoundException("Config item not found: " + configId);
        }
        configRepository.deleteById(configId);
    }

    // ==================== HELPERS ====================

    private void validateGroup(String group) {
        if (!VALID_GROUPS.contains(group)) {
            throw new BadRequestException("Invalid config group: " + group);
        }
    }

    private SystemConfigResponse toResponse(SystemConfig c) {
        return SystemConfigResponse.builder()
                .configId(c.getConfigId())
                .configGroup(c.getConfigGroup())
                .configValue(c.getConfigValue())
                .displayOrder(c.getDisplayOrder())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}