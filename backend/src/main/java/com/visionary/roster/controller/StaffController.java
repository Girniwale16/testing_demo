package com.visionary.roster.controller;

import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.StaffUpdateRequest;
import com.visionary.roster.service.StaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for staff management operations.
 * Handles staff updates, deactivation, retrieval, and listing.
 */
@RestController
@RequestMapping("/api/staff")
@CrossOrigin
public class StaffController {

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
     * Updates staff information.
     * Requires authentication and appropriate permissions.
     *
     * @param id the staff ID
     * @param request the update request containing staff details
     * @param userDetails the authenticated user details
     * @return ResponseEntity containing the updated staff response
     */
    @PutMapping("/{id}")
    public ResponseEntity<StaffResponse> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody StaffUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String requestingUserId = userDetails.getUsername();
        StaffResponse response = staffService.updateStaff(id, request, requestingUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivates a staff member.
     * Operation is idempotent - returns 200 OK even if staff is already inactive.
     *
     * @param id the staff ID to deactivate
     * @param userDetails the authenticated user details
     * @return ResponseEntity with no content on successful deactivation
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateStaff(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String requestingUserId = userDetails.getUsername();
        staffService.deactivateStaff(id, requestingUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves staff details by ID.
     *
     * @param id the staff ID
     * @param userDetails the authenticated user details
     * @return ResponseEntity containing the staff response
     */
    @GetMapping("/{id}")
    public ResponseEntity<StaffResponse> getStaff(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        StaffResponse response = staffService.getStaff(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all active staff members in a facility.
     *
     * @param facilityId the facility ID to filter staff
     * @param userDetails the authenticated user details
     * @return ResponseEntity containing list of active staff responses
     */
    @GetMapping
    public ResponseEntity<List<StaffResponse>> listActiveStaff(
            @RequestParam Long facilityId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<StaffResponse> staffList = staffService.listActiveStaff(facilityId);
        return ResponseEntity.ok(staffList);
    }
}