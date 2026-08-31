package com.visionary.roster.service;

import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.StaffUpdateRequest;
import com.visionary.roster.entity.Staff;
import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.exception.ResourceNotFoundException;
import com.visionary.roster.repository.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing staff operations including updates, deactivation, and retrieval.
 * Enforces role-based authorization and facility scoping for all operations.
 */
@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final FacilityScopingService facilityScopingService;
    private final RoleAuthorizationService roleAuthorizationService;
    private final AuditEmitter auditEmitter;

    /**
     * Constructor-based dependency injection.
     *
     * @param staffRepository the staff repository
     * @param facilityScopingService the facility scoping service
     * @param roleAuthorizationService the role authorization service
     * @param auditEmitter the audit emitter
     */
    public StaffService(StaffRepository staffRepository,
                        FacilityScopingService facilityScopingService,
                        RoleAuthorizationService roleAuthorizationService,
                        AuditEmitter auditEmitter) {
        this.staffRepository = staffRepository;
        this.facilityScopingService = facilityScopingService;
        this.roleAuthorizationService = roleAuthorizationService;
        this.auditEmitter = auditEmitter;
    }

    /**
     * Updates staff information with manager authorization and facility scoping validation.
     *
     * @param staffId the ID of the staff to update
     * @param request the update request containing new staff data
     * @param requestingUserId the ID of the user requesting the update
     * @return StaffResponse containing updated staff information
     * @throws ForbiddenAccessException if user lacks manager role or facility access
     * @throws ResourceNotFoundException if staff not found
     */
    @Transactional
    public StaffResponse updateStaff(Long staffId, StaffUpdateRequest request, Long requestingUserId) {
        try {
            // Enforce manager-only access
            roleAuthorizationService.requireRole(requestingUserId, "MANAGER");
        } catch (ForbiddenAccessException e) {
            throw e;
        }

        // Retrieve Staff entity
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));

        try {
            // Validate facility access for current staff facility
            facilityScopingService.validateFacilityAccess(requestingUserId, staff.getFacility().getId());
        } catch (ForbiddenAccessException e) {
            throw e;
        }

        // If facility is being changed, validate access to new facility
        if (request.getFacilityId() != null && !request.getFacilityId().equals(staff.getFacility().getId())) {
            try {
                facilityScopingService.validateFacilityAccess(requestingUserId, request.getFacilityId());
            } catch (ForbiddenAccessException e) {
                throw e;
            }
        }

        // Validate email uniqueness
        if (request.getEmail() != null && staffRepository.existsByEmailAndIdNot(request.getEmail(), staffId)) {
            throw new IllegalArgumentException("Email already exists for another staff member");
        }

        // Build change map for audit
        Map<String, Object> changeMap = new HashMap<>();

        // Apply field updates and track changes
        if (request.getFirstName() != null && !request.getFirstName().equals(staff.getFirstName())) {
            changeMap.put("firstName", Map.of("old", staff.getFirstName(), "new", request.getFirstName()));
            staff.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null && !request.getLastName().equals(staff.getLastName())) {
            changeMap.put("lastName", Map.of("old", staff.getLastName(), "new", request.getLastName()));
            staff.setLastName(request.getLastName());
        }

        if (request.getEmail() != null && !request.getEmail().equals(staff.getEmail())) {
            changeMap.put("email", Map.of("old", staff.getEmail(), "new", request.getEmail()));
            staff.setEmail(request.getEmail());
        }

        if (request.getRole() != null && !request.getRole().equals(staff.getRole())) {
            changeMap.put("role", Map.of("old", staff.getRole(), "new", request.getRole()));
            staff.setRole(request.getRole());
        }

        if (request.getFacilityId() != null && !request.getFacilityId().equals(staff.getFacility().getId())) {
            changeMap.put("facilityId", Map.of("old", staff.getFacility().getId(), "new", request.getFacilityId()));
            staff.getFacility().setId(request.getFacilityId());
        }

        if (request.getEmploymentStatus() != null && !request.getEmploymentStatus().equals(staff.getEmploymentStatus())) {
            changeMap.put("employmentStatus", Map.of("old", staff.getEmploymentStatus(), "new", request.getEmploymentStatus()));
            staff.setEmploymentStatus(request.getEmploymentStatus());
        }

        // Save updated staff entity
        Staff updatedStaff = staffRepository.save(staff);

        // Emit audit event
        auditEmitter.emitStaffUpdateEvent(staffId, requestingUserId, changeMap);

        // Return response
        return StaffResponse.fromEntity(updatedStaff);
    }

    /**
     * Deactivates a staff member with manager authorization and facility scoping validation.
     * Implements idempotency - returns silently if staff is already deactivated.
     *
     * @param staffId the ID of the staff to deactivate
     * @param requestingUserId the ID of the user requesting the deactivation
     * @throws ForbiddenAccessException if user lacks manager role or facility access
     * @throws ResourceNotFoundException if staff not found
     */
    @Transactional
    public void deactivateStaff(Long staffId, Long requestingUserId) {
        try {
            // Enforce manager-only access
            roleAuthorizationService.requireRole(requestingUserId, "MANAGER");
        } catch (ForbiddenAccessException e) {
            throw e;
        }

        // Retrieve Staff entity
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));

        try {
            // Validate facility access
            facilityScopingService.validateFacilityAccess(requestingUserId, staff.getFacility().getId());
        } catch (ForbiddenAccessException e) {
            throw e;
        }

        // Implement idempotency - return silently if already deactivated
        if (!staff.isActive()) {
            return;
        }

        // Deactivate staff
        staff.deactivate(LocalDate.now());

        // Save deactivated staff entity
        staffRepository.save(staff);

        // Emit audit event
        auditEmitter.emitStaffDeactivateEvent(staffId, requestingUserId, "Manager-initiated deactivation");
    }

    /**
     * Retrieves staff details by ID with facility scoping validation.
     *
     * @param staffId the ID of the staff to retrieve
     * @param requestingUserId the ID of the user requesting the staff details
     * @return StaffResponse containing staff information
     * @throws ForbiddenAccessException if user lacks facility access
     * @throws ResourceNotFoundException if staff not found
     */
    public StaffResponse getStaffById(Long staffId, Long requestingUserId) {
        // Retrieve Staff entity
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));

        try {
            // Validate facility access
            facilityScopingService.validateFacilityAccess(requestingUserId, staff.getFacility().getId());
        } catch (ForbiddenAccessException e) {
            throw e;
        }

        // Return response
        return StaffResponse.fromEntity(staff);
    }

    /**
     * Lists all active staff for a given facility with facility scoping validation.
     *
     * @param facilityId the ID of the facility
     * @param requestingUserId the ID of the user requesting the staff list
     * @return List of StaffResponse containing active staff information
     * @throws ForbiddenAccessException if user lacks facility access
     */
    public List<StaffResponse> listActiveStaff(Long facilityId, Long requestingUserId) {
        try {
            // Validate facility access
            facilityScopingService.validateFacilityAccess(requestingUserId, facilityId);
        } catch (ForbiddenAccessException e) {
            throw e;
        }

        // Retrieve active staff for facility
        List<Staff> activeStaff = staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, "ACTIVE");

        // Convert to response DTOs
        return activeStaff.stream()
                .map(StaffResponse::fromEntity)
                .collect(Collectors.toList());
    }
}