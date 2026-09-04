package com.visionary.roster.controller;

import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.UpdateStaffRequest;
import com.visionary.roster.exception.FacilityAccessDeniedException;
import com.visionary.roster.exception.ResourceNotFoundException;
import com.visionary.roster.service.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping
    public ResponseEntity<StaffResponse> createStaff(@RequestBody CreateStaffRequest request, 
                                                      @RequestHeader("X-Correlation-ID") String correlationId) {
        Long userId = getUserIdFromSecurityContext();
        Long facilityId = request.getFacilityId();
        StaffResponse response = staffService.createStaff(request, facilityId, userId);
        URI location = URI.create("/api/staff/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{staffId}")
    public ResponseEntity<StaffResponse> updateStaff(@PathVariable Long staffId,
                                                      @RequestBody UpdateStaffRequest request,
                                                      @RequestHeader("X-Correlation-ID") String correlationId) {
        Long userId = getUserIdFromSecurityContext();
        StaffResponse response = staffService.updateStaff(staffId, request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{staffId}")
    public ResponseEntity<StaffResponse> getStaff(@PathVariable Long staffId) {
        Long userId = getUserIdFromSecurityContext();
        StaffResponse response = staffService.getStaff(staffId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<StaffResponse>> listStaff(@RequestParam Long facilityId) {
        Long userId = getUserIdFromSecurityContext();
        List<StaffResponse> staffList = staffService.listStaff(facilityId, userId);
        return ResponseEntity.ok(staffList);
    }

    @PostMapping("/{staffId}/deactivate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deactivateStaff(@PathVariable Long staffId) {
        Long userId = getUserIdFromSecurityContext();
        staffService.deactivateStaff(staffId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<StaffResponse>> listActiveStaff(@RequestParam Long facilityId) {
        Long userId = getUserIdFromSecurityContext();
        List<StaffResponse> activeStaffList = staffService.listActiveStaff(facilityId, userId);
        return ResponseEntity.ok(activeStaffList);
    }

    private Long getUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}