package com.visionary.roster.repository;

import com.visionary.roster.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Staff entity operations.
 * Provides CRUD operations and custom query methods for staff management.
 */
@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    /**
     * Retrieves active staff by facility and employment status.
     *
     * @param facilityId the ID of the facility
     * @param employmentStatus the employment status (e.g., "ACTIVE", "INACTIVE")
     * @return list of staff matching the criteria
     */
    List<Staff> findByFacility_FacilityIdAndEmploymentStatus(Long facilityId, String employmentStatus);

    /**
     * Retrieves all staff members for a given facility, regardless of employment status.
     *
     * @param facilityId the ID of the facility
     * @return list of staff belonging to the facility
     */
    List<Staff> findByFacilityId(Long facilityId);

    /**
     * Checks if a staff member with the given email already exists.
     * Used for email uniqueness validation during creation.
     *
     * @param email the email to check
     * @return true if a staff member with this email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Retrieves all staff members by employment status.
     *
     * @param employmentStatus the employment status
     * @return list of staff with the specified employment status
     */
    List<Staff> findByEmploymentStatus(String employmentStatus);

    /**
     * Checks if an email exists for any staff member except the one with the given ID.
     * Used for email uniqueness validation during updates.
     *
     * @param email the email to check
     * @param id the ID of the current staff record to exclude
     * @return true if email exists for another staff member, false otherwise
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Finds a staff member by email address.
     *
     * @param email the email address
     * @return Optional containing the staff if found
     */
    Optional<Staff> findByEmail(String email);
}