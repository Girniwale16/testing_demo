package com.visionary.roster.service;

import com.visionary.roster.audit.AuditEmitter;
import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.StaffUpdateRequest;
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
     * Creates a new staff member with authorization and facility scoping validation.
     *
     * @param request the create staff request containing staff data
     * @param facilityId the ID of the facility where staff will be created
     * @param userId the ID of the user creating the staff
     * @return StaffResponse containing created staff information
     * @throws ForbiddenAccessException if user lacks permission or facility access
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public StaffResponse createStaff(CreateStaffRequest request, Long facilityId, String userId) {
        // Verify user has permission to create staff
        roleAuthorizationService.checkPermission(userId, "STAFF_CREATE");

        // Ensure user can create staff in target facility
        facilityScopingService.validateFacilityAccess(userId, facilityId);

        // Convert CreateStaffRequest to Staff entity
        Staff staff = request.toEntity(facilityId);

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
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("action", "STAFF_CREATED");
        auditData.put("userId", userId);
        auditData.put("facilityId", facilityId);
        auditData.put("staffId", savedStaff.getId());
        auditEmitter.emitStaffCreateEvent(savedStaff.getId(), userId, facilityId);

        // Return StaffResponse
        return StaffResponse.fromEntity(savedStaff);
    }

    /**
     * Updates staff information with authorization and facility scoping validation.
     *
     * @param staffId the ID of the staff to update
     * @param request the update request containing new staff data
     * @param userId the ID of the user requesting the update
     * @return StaffResponse containing updated staff information
     * @throws ForbiddenAccessException if user lacks permission or facility access
     * @throws ResourceNotFoundException if staff not found
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public StaffResponse updateStaff(Long staffId, UpdateStaffRequest request, String userId) {
        // Retrieve existing Staff entity by staffId
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));

        // Verify user has permission to update staff
        roleAuthorizationService.checkPermission(userId, "STAFF_UPDATE");

        // Ensure user can modify staff in this facility
        facilityScopingService.validateFacilityAccess(userId, staff.getFacilityId());

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
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("action", "STAFF_UPDATED");
        auditData.put("userId", userId);
        auditData.put("facilityId", staff.getFacilityId());
        auditData.put("staffId", staffId);
        auditData.put("changedFields", changedFields);
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
    public StaffResponse getStaff(Long staffId, String userId) {
        // Retrieve Staff entity
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));

        // Validate facility access
        facilityScopingService.validateFacilityAccess(userId, staff.getFacilityId());

        // Return StaffResponse
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
    public List<StaffResponse> listStaff(Long facilityId, String userId) {
        // Validate facility access
        facilityScopingService.validateFacilityAccess(userId, facilityId);

        // Query repository for all staff in facility
        List<Staff> staffList = staffRepository.findByFacilityId(facilityId);

        // Map to StaffResponse list
        return staffList.stream()
                .map(StaffResponse::fromEntity)
                .collect(Collectors.toList());
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
            roleAuthorizationService.requireManagerRole();
        } catch (ForbiddenAccessException e) {
            throw e;
        }

        // Retrieve Staff entity
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));

        try {
            // Validate facility access for current staff facility
            facilityScopingService.validateFacilityAccess(staff.getFacility().getFacilityId());
        } catch (ForbiddenAccessException e) {
            throw e;
        }

        // If facility is being changed, validate access to new facility
        if (request.getFacilityId() != null && !request.getFacilityId().equals(staff.getFacility().getFacilityId())) {
            try {
                facilityScopingService.validateFacilityAccess(request.getFacilityId());
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

        if (request.getFacilityId() != null && !request.getFacilityId().equals(staff.getFacility().getFacilityId())) {
            changeMap.put("facilityId", Map.of("old", staff.getFacility().getFacilityId(), "new", request.getFacilityId()));
            staff.getFacility().setFacilityId(request.getFacilityId());
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
            roleAuthorizationService.requireManagerRole();
        } catch (ForbiddenAccessException e) {
            throw e;
        }

        // Retrieve Staff entity
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));

        try {
            // Validate facility access
            facilityScopingService.validateFacilityAccess(staff.getFacility().getFacilityId());
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
            facilityScopingService.validateFacilityAccess(staff.getFacility().getFacilityId());
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
            facilityScopingService.validateFacilityAccess(facilityId);
        } catch (ForbiddenAccessException e) {
            throw e;
        }

        // Retrieve active staff for facility
        List<Staff> activeStaff = staffRepository.findByFacility_FacilityIdAndEmploymentStatus(facilityId, "ACTIVE");

        // Convert to response DTOs
        return activeStaff.stream()
                .map(StaffResponse::fromEntity)
                .collect(Collectors.toList());
    }
}