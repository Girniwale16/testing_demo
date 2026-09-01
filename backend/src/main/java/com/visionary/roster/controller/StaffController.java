package com.visionary.roster.controller;

import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.StaffUpdateRequest;
import com.visionary.roster.dto.UpdateStaffRequest;
import com.visionary.roster.service.StaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

/**
 * REST controller for staff management operations.
 * Handles staff creation, updates, deactivation, retrieval, and listing.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class StaffController {

    private static final Logger logger = LoggerFactory.getLogger(StaffController.class);

    private final StaffService staffService;

    /**
     * Constructor injection for StaffService dependency.
     *
     * @param staffService the staff service
     */
    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    /**
     * Creates a new staff member.
     * Requires MANAGER role.
     *
     * @param request the create staff request containing staff details
     * @param correlationId the correlation ID for request tracing
     * @return ResponseEntity containing the created staff response with 201 CREATED status
     */
    @PostMapping("/staff")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<StaffResponse> createStaff(
            @Valid @RequestBody CreateStaffRequest request,
            @RequestHeader("X-Correlation-ID") String correlationId) {
        Long userId = currentUserId();
        Long facilityId = request.getFacilityId();
        
        logger.info("POST /api/staff - correlationId: {}, facilityId: {}, userId: {}", 
                    correlationId, facilityId, userId);
        
        StaffResponse response = staffService.createStaff(request, facilityId, userId);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/staff/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Updates staff information by staff ID.
     * Requires MANAGER role.
     *
     * @param staffId the staff ID
     * @param request the update request containing staff details
     * @param correlationId the correlation ID for request tracing
     * @return ResponseEntity containing the updated staff response with 200 OK status
     */
    @PutMapping("/staff/{staffId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<StaffResponse> updateStaff(
            @PathVariable Long staffId,
            @Valid @RequestBody UpdateStaffRequest request,
            @RequestHeader("X-Correlation-ID") String correlationId) {
        Long userId = currentUserId();
        
        logger.info("PUT /api/staff/{} - correlationId: {}, staffId: {}, userId: {}", 
                    staffId, correlationId, staffId, userId);
        
        StaffResponse response = staffService.updateStaff(staffId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves staff details by staff ID.
     * Requires authentication.
     *
     * @param staffId the staff ID
     * @return ResponseEntity containing the staff response with 200 OK status
     */
    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<StaffResponse> getStaff(@PathVariable Long staffId) {
        Long userId = currentUserId();
        StaffResponse response = staffService.getStaff(staffId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all staff members in a facility.
     * Requires authentication.
     *
     * @param facilityId the facility ID to filter staff
     * @return ResponseEntity containing list of staff responses with 200 OK status
     */
    @GetMapping("/facilities/{facilityId}/staff")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<StaffResponse>> listStaff(@PathVariable Long facilityId) {
        Long userId = currentUserId();
        List<StaffResponse> staffList = staffService.listStaff(facilityId, userId);
        return ResponseEntity.ok(staffList);
    }

    /**
     * Deactivates a staff member.
     * Operation is idempotent - returns 200 OK even if staff is already inactive.
     *
     * @param id the staff ID to deactivate
     * @return ResponseEntity with no content on successful deactivation
     */
    @PostMapping("/staff/{id}/deactivate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deactivateStaff(@PathVariable Long id) {
        Long requestingUserId = currentUserId();
        staffService.deactivateStaff(id, requestingUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Lists all active staff members in a facility.
     *
     * @param facilityId the facility ID to filter staff
     * @return ResponseEntity containing list of active staff responses
     */
    @GetMapping("/staff")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<StaffResponse>> listActiveStaff(@RequestParam Long facilityId) {
        Long requestingUserId = currentUserId();
        List<StaffResponse> staffList = staffService.listActiveStaff(facilityId, requestingUserId);
        return ResponseEntity.ok(staffList);
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}