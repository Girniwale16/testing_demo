package com.visionary.roster.repository;

import com.visionary.roster.entity.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for StaffMember entity. Provides facility-scoped queries 
 * leveraging composite indexes for optimal performance.
 */
@Repository
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {

    /**
     * Retrieves a staff member by ID.
     * Inherited from JpaRepository but explicitly documented.
     *
     * @param staffMemberId the ID of the staff member
     * @return an Optional containing the staff member if found
     */
    Optional<StaffMember> findById(Long staffMemberId);

    /**
     * Retrieves all staff members for a given facility. Used by FacilityScopingService 
     * for facility-based access control.
     *
     * @param facilityId the ID of the facility
     * @return list of staff members belonging to the facility
     */
    // Leverages composite index idx_staff_member_facility_status
    List<StaffMember> findByFacilityId(Long facilityId);

    /**
     * Retrieves staff members filtered by facility and employment status. Supports queries 
     * for active/inactive/terminated staff rosters.
     *
     * @param facilityId the ID of the facility
     * @param employmentStatus the employment status to filter by
     * @return list of staff members matching the criteria
     */
    // Optimized by composite index on (facility_id, employment_status)
    List<StaffMember> findByFacilityIdAndEmploymentStatus(Long facilityId, String employmentStatus);

    /**
     * Retrieves staff members by facility and name. Supports staff search functionality 
     * within facility scope.
     *
     * @param facilityId the ID of the facility
     * @param name the name of the staff member
     * @return list of staff members matching the criteria
     */
    // Optimized by composite index on (facility_id, name)
    List<StaffMember> findByFacilityIdAndName(Long facilityId, String name);

    /**
     * Saves a staff member entity.
     * Inherited from JpaRepository but explicitly documented.
     *
     * @param staffMember the staff member to save
     * @return the saved staff member
     */
    StaffMember save(StaffMember staffMember);

    /**
     * Deletes a staff member by ID.
     * Inherited from JpaRepository but explicitly documented.
     * 
     * Note: Physical deletion via deleteById() should be avoided. Use employment_status 
     * transition and end_date population for soft-delete.
     *
     * @param staffMemberId the ID of the staff member to delete
     */
    void deleteById(Long staffMemberId);

    // Note: Physical deletion via deleteById() should be avoided. Use employment_status transition and end_date population for soft-delete.
    // MITIGATION: Use identical method naming conventions from UserAccountRepository.java (findByFacilityId pattern) to maintain consistency across repository interfaces and prevent confusion for future developers
    // MITIGATION: Do NOT add custom @Query JPQL for basic finder methods - rely on Spring Data JPA method name derivation to leverage composite indexes automatically and reduce maintenance overhead
    // MITIGATION: Document that all facility-scoped queries must integrate with FacilityScopingService.java for authorization enforcement to prevent unauthorized cross-facility data access
}