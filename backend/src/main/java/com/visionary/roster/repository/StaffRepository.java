package com.visionary.roster.repository;

import com.visionary.roster.entity.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for StaffMember entity operations.
 * 
 * <p>This repository provides CRUD operations and custom query methods for StaffMember entities.
 * All finder methods enforce facility-scoped multi-tenancy to ensure data isolation between facilities.
 * 
 * <p>The employmentStatus parameter in query methods should default to 'active' in the service layer
 * when filtering staff members by their current employment status.
 * 
 * @author Visionary Roster System
 * @see StaffMember
 * @see JpaRepository
 */
@Repository
public interface StaffRepository extends JpaRepository<StaffMember, Long> {

    /**
     * Retrieves all staff members associated with a specific facility.
     * This method enforces facility-scoped multi-tenancy.
     * 
     * @param facilityId the ID of the facility
     * @return a list of staff members belonging to the specified facility
     */
    List<StaffMember> findByFacilityId(Long facilityId);

    /**
     * Retrieves staff members filtered by facility and employment status.
     * This method enforces facility-scoped multi-tenancy.
     * 
     * <p>Note: The employmentStatus parameter should default to 'active' in the service layer
     * when not explicitly specified by the caller.
     * 
     * @param facilityId the ID of the facility
     * @param employmentStatus the employment status to filter by (e.g., 'active', 'inactive', 'terminated')
     * @return a list of staff members matching the facility and employment status criteria
     */
    List<StaffMember> findByFacilityIdAndEmploymentStatus(Long facilityId, String employmentStatus);

    /**
     * Retrieves a single staff member by ID within a specific facility scope.
     * This method enforces facility-scoped multi-tenancy for single record lookup.
     * 
     * @param id the ID of the staff member
     * @param facilityId the ID of the facility
     * @return an Optional containing the staff member if found within the facility scope, empty otherwise
     */
    Optional<StaffMember> findByIdAndFacilityId(Long id, Long facilityId);
}