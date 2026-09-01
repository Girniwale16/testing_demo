package com.visionary.roster.service;

import com.visionary.roster.audit.AuditEmitter;
import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.UpdateStaffRequest;
import com.visionary.roster.model.Staff;
import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.exception.ResourceNotFoundException;
import com.visionary.roster.repository.StaffRepository;
import com.visionary.roster.security.FacilityScopingService;
import com.visionary.roster.security.RoleAuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing staff operations including creation, updates, deactivation, and retrieval.
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
     * Creates a new staff member with manager authorization and facility scoping validation.
     *
     * @param request the create staff request containing staff data
     * @param facilityId the ID of the facility where staff will be created
     * @param userId the ID of the user creating the staff
     * @return StaffResponse containing created staff information
     * @throws ForbiddenAccessException if user lacks manager role or facility access
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public StaffResponse createStaff(CreateStaffRequest request, Long facilityId, Long userId) {
        // Enforce manager-only access
        roleAuthorizationService.requireManagerRole();

        // Ensure user can create staff in target facility
        facilityScopingService.validateFacilityAccess(facilityId);

        // Validate email uniqueness
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists for another staff member");
        }

        // Convert CreateStaffRequest to Staff entity
        Staff staff = Staff.toEntity(request, facilityId);

        // Validate employment_status transitions (new staff must start with ACTIVE or PENDING status)
        String employmentStatus = staff.getEmploymentStatus();
        if (employmentStatus != null &&
            !employmentStatus.equals("ACTIVE") &&
            !employmentStatus.equals("PENDING")) {
            throw new IllegalArgumentException("New staff must start with ACTIVE or PENDING status");
        }

        // Validate date ranges (start_date must be before end_date if end_date is provided)
        if (staff.getStartDate() != null && staff.getEndDate() != null) {
            if (!staff.getStartDate().isBefore(staff.getEndDate())) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
        }

        // Save Staff entity to repository
        Staff savedStaff = staffRepository.save(staff);

        // Emit audit event
        Map<String, Object> auditMetadata = new HashMap<>();
        auditMetadata.put("facilityId", facilityId);
        auditEmitter.emitAuditEvent("STAFF_CREATED", "STAFF", savedStaff.getId(), userId, auditMetadata);

        // Return StaffResponse
        return StaffResponse.fromEntity(savedStaff);
    }

    /**
     * Updates staff information with manager authorization and facility scoping validation.
     *
     * @param staffId the ID of the staff to update
     * @param request the update request containing new staff data
     * @param userId the ID of the user requesting the update
     * @return StaffResponse containing updated staff information
     * @throws ForbiddenAccessException if user lacks manager role or facility access
     * @throws ResourceNotFoundException if staff not found
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public StaffResponse updateStaff(Long staffId, UpdateStaffRequest request, Long userId) {
        // Enforce manager-only access
        roleAuthorizationService.requireManagerRole();

        // Retrieve existing Staff entity
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));

        // Ensure user can modify staff in this facility
        facilityScopingService.validateFacilityAccess(staff.getFacilityId());

        // Track changed fields for audit
        Map<String, Object> changedFields = new HashMap<>();

        // Store old employment status for validation
        String oldEmploymentStatus = staff.getEmploymentStatus();

        // Apply updates from UpdateStaffRequest to Staff entity
        staff.updateFromRequest(request);

        // Validate employment_status transitions
        String newEmploymentStatus = staff.getEmploymentStatus();
        if (newEmploymentStatus != null && !newEmploymentStatus.equals(oldEmploymentStatus)) {
            // Cannot change from TERMINATED to ACTIVE without proper workflow
            if ("TERMINATED".equals(oldEmploymentStatus) && "ACTIVE".equals(newEmploymentStatus)) {
                throw new IllegalArgumentException("Cannot change employment status from TERMINATED to ACTIVE without proper workflow");
            }
            changedFields.put("employmentStatus", Map.of("old", oldEmploymentStatus, "new", newEmploymentStatus));
        }

        // Validate date ranges after updates
        if (staff.getStartDate() != null && staff.getEndDate() != null) {
            if (!staff.getStartDate().isBefore(staff.getEndDate())) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
        }

        // Save updated Staff entity
        Staff updatedStaff = staffRepository.save(staff);

        // Emit audit event
        auditEmitter.emitStaffUpdateEvent(staffId, userId, changedFields);

        // Return StaffResponse
        return StaffResponse.fromEntity(updatedStaff);
    }

    /**
     * Retrieves a single staff record with facility scoping validation.
     *
     * @param staffId the ID of the staff to retrieve
     * @param userId the ID of the user requesting the staff details
     * @return StaffResponse containing staff information
     * @throws ForbiddenAccessException if user lacks facility access
     * @throws ResourceNotFoundException if staff not found
     */
    public StaffResponse getStaff(Long staffId, Long userId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));

        facilityScopingService.validateFacilityAccess(staff.getFacilityId());

        return StaffResponse.fromEntity(staff);
    }

    /**
     * Retrieves all staff in a facility with facility scoping validation.
     *
     * @param facilityId the ID of the facility
     * @param userId the ID of the user requesting the staff list
     * @return List of StaffResponse containing staff information
     * @throws ForbiddenAccessException if user lacks facility access
     */
    public List<StaffResponse> listStaff(Long facilityId, Long userId) {
        facilityScopingService.validateFacilityAccess(facilityId);

        List<Staff> staffList = staffRepository.findByFacilityId(facilityId);

        return staffList.stream()
                .map(StaffResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Deactivates a staff member with manager authorization and facility scoping validation.
     * 
     * <p>This method enforces MANAGER role requirement and validates facility access before
     * deactivating a staff member. The method implements idempotency - if the staff member
     * is already inactive, the method returns successfully without throwing an exception.</p>
     * 
     * <p>Authorization and Access Control:</p>
     * <ul>
     *   <li>Requires MANAGER role via roleAuthorizationService.requireManagerRole(userId)</li>
     *   <li>Validates facility access via facilityScopingService.validateFacilityAccess(userId, facilityId)</li>
     *   <li>Throws ForbiddenAccessException if role or facility access validation fails</li>
     *   <li>Throws FacilityAccessDeniedException if facility scoping validation fails</li>
     * </ul>
     * 
     * <p>Idempotency Behavior:</p>
     * <ul>
     *   <li>If staff.isActive() is already false, the method skips setActive(false)</li>
     *   <li>No exception is thrown for already-inactive staff</li>
     *   <li>Returns successfully to support idempotent API calls</li>
     * </ul>
     * 
     * <p>Audit Trail:</p>
     * <ul>
     *   <li>Emits audit event via auditEmitter.emitStaffUpdateEvent(staffId, userId, metadata)</li>
     *   <li>Metadata includes: userRole, requiredRole="MANAGER", authorizationResult="AUTHORIZED", action="DEACTIVATE"</li>
     * </ul>
     *
     * @param staffId the ID of the staff to deactivate
     * @param userId the ID of the user requesting the deactivation (must have MANAGER role)
     * @throws ForbiddenAccessException if user lacks manager role
     * @throws com.visionary.roster.exception.FacilityAccessDeniedException if user lacks facility access
     * @throws ResourceNotFoundException if staff not found
     */
    @Transactional
    public void deactivateStaff(Long staffId, Long userId) {
        // Validate MANAGER role with userId parameter
        roleAuthorizationService.requireManagerRole(userId);

        // Retrieve Staff entity by staffId
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));

        // Extract facilityId and validate facility access with both userId and facilityId
        Long facilityId = staff.getFacilityId();
        facilityScopingService.validateFacilityAccess(userId, facilityId);

        // Implement idempotency: if staff is already inactive, skip setActive(false) but still return success
        if (!staff.isActive()) {
            // Staff is already inactive, return without exception (idempotent behavior)
            return;
        }

        // Set staff as inactive
        staff.setActive(false);

        // Save updated Staff entity within @Transactional context
        staffRepository.save(staff);

        // Create metadata Map with required keys
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userRole", "MANAGER");
        metadata.put("requiredRole", "MANAGER");
        metadata.put("authorizationResult", "AUTHORIZED");
        metadata.put("action", "DEACTIVATE");

        // Emit audit event for staff update
        auditEmitter.emitStaffUpdateEvent(staffId, userId, metadata);
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
        facilityScopingService.validateFacilityAccess(facilityId);

        List<Staff> activeStaff = staffRepository.findByFacility_FacilityIdAndEmploymentStatus(facilityId, "ACTIVE");

        return activeStaff.stream()
                .map(StaffResponse::fromEntity)
                .collect(Collectors.toList());
    }
}