package com.mpp.rental.controller;

import com.mpp.rental.dto.ApiResponse;
import com.mpp.rental.dto.SystemConfigRequest;
import com.mpp.rental.dto.SystemConfigResponse;
import com.mpp.rental.model.SystemConfig;
import com.mpp.rental.service.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService configService;

    // ==================== PUBLIC CONFIG ENDPOINTS ====================
    // Used by MPP/BO forms to populate dropdowns with active values

    @GetMapping("/api/config/{group}")
    public ResponseEntity<ApiResponse<List<String>>> getActiveValues(
            @PathVariable String group) {
        try {
            List<String> values = configService.getActiveValues(group.toUpperCase());
            return ResponseEntity.ok(ApiResponse.success("Config retrieved", values));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== SUPER ADMIN MANAGEMENT ====================

    /** GET all config items across all groups */
    @GetMapping("/api/superadmin/config")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SystemConfigResponse>>> getAll() {
        try {
            return ResponseEntity.ok(ApiResponse.success("Configs retrieved", configService.getAll()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** GET items by group */
    @GetMapping("/api/superadmin/config/{group}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SystemConfigResponse>>> getByGroup(
            @PathVariable String group) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Config retrieved", configService.getAllByGroup(group.toUpperCase())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** POST create new config item */
    @PostMapping("/api/superadmin/config")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> create(
            @Valid @RequestBody SystemConfigRequest request) {
        try {
            SystemConfigResponse created = configService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Config item created", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** PUT update config item */
    @PutMapping("/api/superadmin/config/{configId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> update(
            @PathVariable Integer configId,
            @Valid @RequestBody SystemConfigRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Config item updated", configService.update(configId, request)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** PATCH toggle active/inactive */
    @PatchMapping("/api/superadmin/config/{configId}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> toggleActive(
            @PathVariable Integer configId) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Status toggled", configService.toggleActive(configId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** DELETE config item */
    @DeleteMapping("/api/superadmin/config/{configId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer configId) {
        try {
            configService.delete(configId);
            return ResponseEntity.ok(ApiResponse.success("Config item deleted", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== MPP MANAGEMENT (Super Admin only) ====================

    /** Convenience: list all group names */
    @GetMapping("/api/superadmin/config-groups")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<String>>> getGroups() {
        return ResponseEntity.ok(ApiResponse.success("Groups", List.of(
                SystemConfig.GROUP_BUSINESS_CATEGORY,
                SystemConfig.GROUP_FACILITY_TYPE,
                SystemConfig.GROUP_FACILITY_SIZE,
                SystemConfig.GROUP_FACILITY_USAGE,
                SystemConfig.GROUP_EVENT_TYPE
        )));
    }
}