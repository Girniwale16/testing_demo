package com.visionary.roster.service;

import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.entity.StaffMember;
import com.visionary.roster.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing staff members within the roster system.
 * 
 * <p>All methods in this service enforce manager-only RBAC (Role-Based Access Control)
 * and facility scoping to ensure that operations are performed only by authorized
 * managers within their assigned facility context.</p>
 * 
 * <p>Staff members are available for shift assignment immediately after creation.
 * No additional status flag is needed to mark availability.</p>
 */
@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final FacilityScopingService facilityScopingService;
    private final RoleAuthorizationService roleAuthorizationService;

    /**
     * Constructor injection for required dependencies.
     *
     * @param staffRepository the repository for staff member persistence
     * @param facilityScopingService the service for facility context management
     * @param roleAuthorizationService the service for role-based authorization
     */
    @Autowired
    public StaffService(StaffRepository staffRepository,
                        FacilityScopingService facilityScopingService,
                        RoleAuthorizationService roleAuthorizationService) {
        this.staffRepository = staffRepository;
        this.facilityScopingService = facilityScopingService;
        this.roleAuthorizationService = roleAuthorizationService;
    }

    /**
     * Creates a new staff member within the current user's facility context.
     * 
     * <p>This method enforces manager-only RBAC and facility scoping. The staff member
     * is available for shift assignment immediately after creation.</p>
     *
     * @param request the staff creation request containing staff details
     * @return StaffResponse containing the created staff member details
     * @throws IllegalArgumentException if end date is before start date
     */
    public StaffResponse createStaff(CreateStaffRequest request) {
        roleAuthorizationService.requireManagerRole();
        
        Long facilityId = facilityScopingService.getCurrentUserFacilityId();
        
        // Validate that if both startDate and endDate are provided, endDate >= startDate
        if (request.getStartDate() != null && request.getEndDate() != null) {
            LocalDate startDate = request.getStartDate();
            LocalDate endDate = request.getEndDate();
            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("End date must be on or after start date");
            }
        }
        
        StaffMember entity = request.toEntity(facilityId);
        
        StaffMember savedEntity = staffRepository.save(entity);
        
        // TODO: Emit audit event for staff creation - TBD pending audit-emitter interface implementation
        
        return StaffResponse.fromEntity(savedEntity);
    }

    /**
     * Retrieves a list of staff members filtered by employment status within the
     * current user's facility context.
     * 
     * <p>This method enforces manager-only RBAC and facility scoping. If no employment
     * status is provided, defaults to 'active' status.</p>
     *
     * @param employmentStatus the employment status filter (defaults to 'active' if null or empty)
     * @return List of StaffResponse objects matching the criteria
     */
    public List<StaffResponse> listStaff(String employmentStatus) {
        roleAuthorizationService.requireManagerRole();
        
        Long facilityId = facilityScopingService.getCurrentUserFacilityId();
        
        // Default employmentStatus to 'active' if null or empty
        if (employmentStatus == null || employmentStatus.isEmpty()) {
            employmentStatus = "active";
        }
        
        List<StaffMember> staffMembers = staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, employmentStatus);
        
        return staffMembers.stream()
                .map(StaffResponse::fromEntity)
                .collect(Collectors.toList());
    }
}