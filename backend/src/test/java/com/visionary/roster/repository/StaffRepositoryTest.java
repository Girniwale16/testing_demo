package com.visionary.roster.repository;

import com.visionary.roster.entity.StaffMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for StaffRepository.
 * 
 * <p>This test class verifies all custom query methods in StaffRepository,
 * with emphasis on facility-scoped multi-tenancy enforcement and employment status filtering.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("StaffRepository Unit Tests")
class StaffRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StaffRepository staffRepository;

    private StaffMember activeStaffFacility1;
    private StaffMember inactiveStaffFacility1;
    private StaffMember activeStaffFacility2;
    private StaffMember terminatedStaffFacility1;

    @BeforeEach
    void setUp() {
        // Setup test data for facility 1
        activeStaffFacility1 = new StaffMember();
        activeStaffFacility1.setFacilityId(1L);
        activeStaffFacility1.setEmploymentStatus("active");
        activeStaffFacility1.setFirstName("John");
        activeStaffFacility1.setLastName("Doe");
        entityManager.persist(activeStaffFacility1);

        inactiveStaffFacility1 = new StaffMember();
        inactiveStaffFacility1.setFacilityId(1L);
        inactiveStaffFacility1.setEmploymentStatus("inactive");
        inactiveStaffFacility1.setFirstName("Jane");
        inactiveStaffFacility1.setLastName("Smith");
        entityManager.persist(inactiveStaffFacility1);

        terminatedStaffFacility1 = new StaffMember();
        terminatedStaffFacility1.setFacilityId(1L);
        terminatedStaffFacility1.setEmploymentStatus("terminated");
        terminatedStaffFacility1.setFirstName("Bob");
        terminatedStaffFacility1.setLastName("Johnson");
        entityManager.persist(terminatedStaffFacility1);

        // Setup test data for facility 2
        activeStaffFacility2 = new StaffMember();
        activeStaffFacility2.setFacilityId(2L);
        activeStaffFacility2.setEmploymentStatus("active");
        activeStaffFacility2.setFirstName("Alice");
        activeStaffFacility2.setLastName("Williams");
        entityManager.persist(activeStaffFacility2);

        entityManager.flush();
    }

    @Test
    @DisplayName("findByFacilityId should return all staff members for a specific facility")
    void testFindByFacilityId_ReturnsAllStaffForFacility() {
        // When
        List<StaffMember> result = staffRepository.findByFacilityId(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).extracting(StaffMember::getFacilityId)
                .containsOnly(1L);
        assertThat(result).extracting(StaffMember::getEmploymentStatus)
                .containsExactlyInAnyOrder("active", "inactive", "terminated");
    }

    @Test
    @DisplayName("findByFacilityId should enforce facility-scoped multi-tenancy")
    void testFindByFacilityId_EnforcesFacilityScoping() {
        // When
        List<StaffMember> facility1Staff = staffRepository.findByFacilityId(1L);
        List<StaffMember> facility2Staff = staffRepository.findByFacilityId(2L);

        // Then
        assertThat(facility1Staff).hasSize(3);
        assertThat(facility2Staff).hasSize(1);
        assertThat(facility1Staff).extracting(StaffMember::getFacilityId)
                .containsOnly(1L);
        assertThat(facility2Staff).extracting(StaffMember::getFacilityId)
                .containsOnly(2L);
    }

    @Test
    @DisplayName("findByFacilityId should return empty list when no staff exists for facility")
    void testFindByFacilityId_ReturnsEmptyListWhenNoStaffExists() {
        // When
        List<StaffMember> result = staffRepository.findByFacilityId(999L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFacilityId should handle null facilityId gracefully")
    void testFindByFacilityId_HandlesNullFacilityId() {
        // When
        List<StaffMember> result = staffRepository.findByFacilityId(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should return staff filtered by status")
    void testFindByFacilityIdAndEmploymentStatus_ReturnsFilteredStaff() {
        // When
        List<StaffMember> activeStaff = staffRepository.findByFacilityIdAndEmploymentStatus(1L, "active");

        // Then
        assertThat(activeStaff).isNotNull();
        assertThat(activeStaff).hasSize(1);
        assertThat(activeStaff.get(0).getEmploymentStatus()).isEqualTo("active");
        assertThat(activeStaff.get(0).getFacilityId()).isEqualTo(1L);
        assertThat(activeStaff.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should enforce facility-scoped multi-tenancy")
    void testFindByFacilityIdAndEmploymentStatus_EnforcesFacilityScoping() {
        // When
        List<StaffMember> facility1Active = staffRepository.findByFacilityIdAndEmploymentStatus(1L, "active");
        List<StaffMember> facility2Active = staffRepository.findByFacilityIdAndEmploymentStatus(2L, "active");

        // Then
        assertThat(facility1Active).hasSize(1);
        assertThat(facility2Active).hasSize(1);
        assertThat(facility1Active.get(0).getFacilityId()).isEqualTo(1L);
        assertThat(facility2Active.get(0).getFacilityId()).isEqualTo(2L);
        assertThat(facility1Active.get(0).getFirstName()).isEqualTo("John");
        assertThat(facility2Active.get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should return inactive staff")
    void testFindByFacilityIdAndEmploymentStatus_ReturnsInactiveStaff() {
        // When
        List<StaffMember> inactiveStaff = staffRepository.findByFacilityIdAndEmploymentStatus(1L, "inactive");

        // Then
        assertThat(inactiveStaff).isNotNull();
        assertThat(inactiveStaff).hasSize(1);
        assertThat(inactiveStaff.get(0).getEmploymentStatus()).isEqualTo("inactive");
        assertThat(inactiveStaff.get(0).getFirstName()).isEqualTo("Jane");
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should return terminated staff")
    void testFindByFacilityIdAndEmploymentStatus_ReturnsTerminatedStaff() {
        // When
        List<StaffMember> terminatedStaff = staffRepository.findByFacilityIdAndEmploymentStatus(1L, "terminated");

        // Then
        assertThat(terminatedStaff).isNotNull();
        assertThat(terminatedStaff).hasSize(1);
        assertThat(terminatedStaff.get(0).getEmploymentStatus()).isEqualTo("terminated");
        assertThat(terminatedStaff.get(0).getFirstName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should return empty list when no match")
    void testFindByFacilityIdAndEmploymentStatus_ReturnsEmptyListWhenNoMatch() {
        // When
        List<StaffMember> result = staffRepository.findByFacilityIdAndEmploymentStatus(1L, "on-leave");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should handle null facilityId")
    void testFindByFacilityIdAndEmploymentStatus_HandlesNullFacilityId() {
        // When
        List<StaffMember> result = staffRepository.findByFacilityIdAndEmploymentStatus(null, "active");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should handle null employmentStatus")
    void testFindByFacilityIdAndEmploymentStatus_HandlesNullEmploymentStatus() {
        // When
        List<StaffMember> result = staffRepository.findByFacilityIdAndEmploymentStatus(1L, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should be case-sensitive for status")
    void testFindByFacilityIdAndEmploymentStatus_IsCaseSensitive() {
        // When
        List<StaffMember> upperCaseResult = staffRepository.findByFacilityIdAndEmploymentStatus(1L, "ACTIVE");
        List<StaffMember> lowerCaseResult = staffRepository.findByFacilityIdAndEmploymentStatus(1L, "active");

        // Then
        assertThat(upperCaseResult).isEmpty();
        assertThat(lowerCaseResult).hasSize(1);
    }

    @Test
    @DisplayName("findByIdAndFacilityId should return staff member when found in facility scope")
    void testFindByIdAndFacilityId_ReturnsStaffMemberWhenFound() {
        // When
        Optional<StaffMember> result = staffRepository.findByIdAndFacilityId(
                activeStaffFacility1.getId(), 1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(activeStaffFacility1.getId());
        assertThat(result.get().getFacilityId()).isEqualTo(1L);
        assertThat(result.get().getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("findByIdAndFacilityId should enforce facility-scoped multi-tenancy")
    void testFindByIdAndFacilityId_EnforcesFacilityScoping() {
        // When - Try to access facility 1 staff with facility 2 scope
        Optional<StaffMember> result = staffRepository.findByIdAndFacilityId(
                activeStaffFacility1.getId(), 2L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndFacilityId should return empty when staff member does not exist")
    void testFindByIdAndFacilityId_ReturnsEmptyWhenNotFound() {
        // When
        Optional<StaffMember> result = staffRepository.findByIdAndFacilityId(999L, 1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndFacilityId should handle null id")
    void testFindByIdAndFacilityId_HandlesNullId() {
        // When
        Optional<StaffMember> result = staffRepository.findByIdAndFacilityId(null, 1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndFacilityId should handle null facilityId")
    void testFindByIdAndFacilityId_HandlesNullFacilityId() {
        // When
        Optional<StaffMember> result = staffRepository.findByIdAndFacilityId(
                activeStaffFacility1.getId(), null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndFacilityId should handle both null parameters")
    void testFindByIdAndFacilityId_HandlesBothNullParameters() {
        // When
        Optional<StaffMember> result = staffRepository.findByIdAndFacilityId(null, null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndFacilityId should work with different employment statuses")
    void testFindByIdAndFacilityId_WorksWithDifferentEmploymentStatuses() {
        // When
        Optional<StaffMember> activeResult = staffRepository.findByIdAndFacilityId(
                activeStaffFacility1.getId(), 1L);
        Optional<StaffMember> inactiveResult = staffRepository.findByIdAndFacilityId(
                inactiveStaffFacility1.getId(), 1L);
        Optional<StaffMember> terminatedResult = staffRepository.findByIdAndFacilityId(
                terminatedStaffFacility1.getId(), 1L);

        // Then
        assertThat(activeResult).isPresent();
        assertThat(activeResult.get().getEmploymentStatus()).isEqualTo("active");
        assertThat(inactiveResult).isPresent();
        assertThat(inactiveResult.get().getEmploymentStatus()).isEqualTo("inactive");
        assertThat(terminatedResult).isPresent();
        assertThat(terminatedResult.get().getEmploymentStatus()).isEqualTo("terminated");
    }

    @Test
    @DisplayName("Repository should extend JpaRepository and inherit CRUD operations")
    void testRepository_ExtendsJpaRepository() {
        // When
        StaffMember newStaff = new StaffMember();
        newStaff.setFacilityId(3L);
        newStaff.setEmploymentStatus("active");
        newStaff.setFirstName("Test");
        newStaff.setLastName("User");
        
        StaffMember saved = staffRepository.save(newStaff);
        Optional<StaffMember> found = staffRepository.findById(saved.getId());

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Repository should support delete operations")
    void testRepository_SupportsDeleteOperations() {
        // Given
        Long staffId = activeStaffFacility1.getId();

        // When
        staffRepository.deleteById(staffId);
        Optional<StaffMember> result = staffRepository.findById(staffId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Repository should support count operations")
    void testRepository_SupportsCountOperations() {
        // When
        long count = staffRepository.count();

        // Then
        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("Naming convention should follow UserAccountRepository pattern")
    void testNamingConvention_FollowsEstablishedPattern() {
        // This test verifies that the method naming follows the pattern:
        // findByFacilityId() - consistent with UserAccountRepository.findByFacilityId()
        
        // When
        List<StaffMember> result = staffRepository.findByFacilityId(1L);

        // Then - Method exists and works as expected
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }
}