package com.visionary.roster.service;

import com.visionary.roster.audit.AuditEmitter;
import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.UpdateStaffRequest;
import com.visionary.roster.model.Facility;
import com.visionary.roster.model.Staff;
import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.exception.ResourceNotFoundException;
import com.visionary.roster.repository.FacilityRepository;
import com.visionary.roster.repository.StaffRepository;
import com.visionary.roster.security.FacilityScopingService;
import com.visionary.roster.security.RoleAuthorizationService;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
 * All methods include correlation ID tracking via MDC context.
 */
@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final FacilityRepository facilityRepository;
    private final FacilityScopingService facilityScopingService;
    private final RoleAuthorizationService roleAuthorizationService;
    private final AuditEmitter auditEmitter;

    /**
     * Constructor-based dependency injection.
     *
     * @param staffRepository the staff repository
     * @param facilityRepository the facility repository
     * @param facilityScopingService the facility scoping service
     * @param roleAuthorizationService the role authorization service
     * @param auditEmitter the audit emitter
     */
    public StaffService(StaffRepository staffRepository,
                        FacilityRepository facilityRepository,
                        FacilityScopingService facilityScopingService,
                        RoleAuthorizationService roleAuthorizationService,
                        AuditEmitter auditEmitter) {
        this.staffRepository = staffRepository;
        this.facilityRepository = facilityRepository;
        this.facilityScopingService = facilityScopingService;
        this.roleAuthorizationService = roleAuthorizationService;
        this.auditEmitter = auditEmitter;
    }

    /**
     * Lists staff with pagination and optional deactivated filter.
     * Filters by is_deactivated column when includeDeactivated is false.
     * Includes correlation ID tracking via MDC context.
     *
     * @param page the page number (zero-based)
     * @param size the page size
     * @param includeDeactivated whether to include deactivated staff
     * @return Page of Staff entities
     */
    public Page<Staff> listStaff(int page, int size, boolean includeDeactivated) {
        String correlationId = MDC.get("correlationId");
        Pageable pageable = PageRequest.of(page, size);
        
        if (includeDeactivated) {
            return staffRepository.findAll(pageable);
        } else {
            return staffRepository.findByIsDeactivated(false, pageable);
        }
    }

    /**
     * Creates a new staff member.
     * Includes correlation ID tracking via MDC context.
     *
     * @param request the create staff request containing staff data
     * @return Staff entity
     */
    @Transactional
    public Staff createStaff(CreateStaffRequest request) {
        String correlationId = MDC.get("correlationId");
        
        // Validate email uniqueness
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists for another staff member");
        }

        // Retrieve facility
        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Facility", request.getFacilityId()));

        // Create new staff entity
        Staff staff = new Staff();
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setEmail(request.getEmail());
        staff.setRole(request.getRole());
        staff.setFacility(facility);
        staff.setEmploymentStatus(request.getEmploymentStatus());
        staff.setActive(true);
        staff.setDeactivated(false);

        // Save staff entity
        Staff savedStaff = staffRepository.save(staff);

        // Emit audit event
        auditEmitter.emitStaffCreateEvent(savedStaff.getId(), correlationId);

        return savedStaff;
    }

    /**
     * Updates staff information.
     * Includes correlation ID tracking via MDC context.
     *
     * @param id the ID of the staff to update
     * @param request the update request containing new staff data
     * @return Staff entity
     * @throws ResourceNotFoundException if staff not found
     */
    @Transactional
    public Staff updateStaff(Long id, UpdateStaffRequest request) {
        String correlationId = MDC.get("correlationId");

        // Retrieve Staff entity
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));

        // Validate email uniqueness if email is being changed
        if (request.getEmail() != null && !request.getEmail().equals(staff.getEmail())) {
            if (staffRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new IllegalArgumentException("Email already exists for another staff member");
            }
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
            Facility newFacility = facilityRepository.findById(request.getFacilityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Facility", request.getFacilityId()));
            changeMap.put("facilityId", Map.of("old", staff.getFacility().getFacilityId(), "new", request.getFacilityId()));
            staff.setFacility(newFacility);
        }

        if (request.getEmploymentStatus() != null && !request.getEmploymentStatus().equals(staff.getEmploymentStatus())) {
            changeMap.put("employmentStatus", Map.of("old", staff.getEmploymentStatus(), "new", request.getEmploymentStatus()));
            staff.setEmploymentStatus(request.getEmploymentStatus());
        }

        // Save updated staff entity
        Staff updatedStaff = staffRepository.save(staff);

        // Emit audit event
        auditEmitter.emitStaffUpdateEvent(id, correlationId, changeMap);

        return updatedStaff;
    }

    /**
     * Deactivates a staff member by setting is_deactivated = true.
     * Emits audit event with correlation ID tracking via MDC context.
     *
     * @param id the ID of the staff to deactivate
     * @throws ResourceNotFoundException if staff not found
     */
    @Transactional
    public void deactivateStaff(Long id) {
        String correlationId = MDC.get("correlationId");

        // Retrieve Staff entity
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));

        // Set is_deactivated = true
        staff.setDeactivated(true);
        staff.setActive(false);
        staff.setDeactivationDate(LocalDate.now());

        // Save deactivated staff entity
        staffRepository.save(staff);

        // Emit audit event
        auditEmitter.emitStaffDeactivateEvent(id, correlationId, "Staff deactivation");
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
        String correlationId = MDC.get("correlationId");
        
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
        String correlationId = MDC.get("correlationId");
        
        try {
            // Validate facility access
            facilityScopingService.validateFacilityAccess(facilityId);
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

    // No-op comment: StaffService business logic is ready for StaffController consumption without modifications.
}