package com.visionary.roster.repository;

import com.visionary.roster.entity.Staff;
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
 * Unit tests for StaffRepository interface.
 * Tests all custom query methods for staff management operations.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("StaffRepository Tests")
class StaffRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StaffRepository staffRepository;

    private Staff activeStaff1;
    private Staff activeStaff2;
    private Staff inactiveStaff;
    private Staff differentFacilityStaff;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        staffRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Setup test data for facility 1
        activeStaff1 = new Staff();
        activeStaff1.setFacilityId(1L);
        activeStaff1.setEmploymentStatus("ACTIVE");
        activeStaff1.setEmail("active1@example.com");
        activeStaff1.setFirstName("John");
        activeStaff1.setLastName("Doe");
        activeStaff1 = entityManager.persistAndFlush(activeStaff1);

        activeStaff2 = new Staff();
        activeStaff2.setFacilityId(1L);
        activeStaff2.setEmploymentStatus("ACTIVE");
        activeStaff2.setEmail("active2@example.com");
        activeStaff2.setFirstName("Jane");
        activeStaff2.setLastName("Smith");
        activeStaff2 = entityManager.persistAndFlush(activeStaff2);

        inactiveStaff = new Staff();
        inactiveStaff.setFacilityId(1L);
        inactiveStaff.setEmploymentStatus("INACTIVE");
        inactiveStaff.setEmail("inactive@example.com");
        inactiveStaff.setFirstName("Bob");
        inactiveStaff.setLastName("Johnson");
        inactiveStaff = entityManager.persistAndFlush(inactiveStaff);

        // Setup test data for facility 2
        differentFacilityStaff = new Staff();
        differentFacilityStaff.setFacilityId(2L);
        differentFacilityStaff.setEmploymentStatus("ACTIVE");
        differentFacilityStaff.setEmail("facility2@example.com");
        differentFacilityStaff.setFirstName("Alice");
        differentFacilityStaff.setLastName("Williams");
        differentFacilityStaff = entityManager.persistAndFlush(differentFacilityStaff);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find active staff by facility ID and employment status")
    void testFindByFacilityIdAndEmploymentStatus_ActiveStaff() {
        // When
        List<Staff> result = staffRepository.findByFacilityIdAndEmploymentStatus(1L, "ACTIVE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Staff::getEmail)
                .containsExactlyInAnyOrder("active1@example.com", "active2@example.com");
        assertThat(result).allMatch(staff -> staff.getFacilityId().equals(1L));
        assertThat(result).allMatch(staff -> staff.getEmploymentStatus().equals("ACTIVE"));
    }

    @Test
    @DisplayName("Should find inactive staff by facility ID and employment status")
    void testFindByFacilityIdAndEmploymentStatus_InactiveStaff() {
        // When
        List<Staff> result = staffRepository.findByFacilityIdAndEmploymentStatus(1L, "INACTIVE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("inactive@example.com");
        assertThat(result.get(0).getFacilityId()).isEqualTo(1L);
        assertThat(result.get(0).getEmploymentStatus()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("Should return empty list when no staff matches facility and status")
    void testFindByFacilityIdAndEmploymentStatus_NoMatch() {
        // When
        List<Staff> result = staffRepository.findByFacilityIdAndEmploymentStatus(999L, "ACTIVE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find staff by ID and facility ID")
    void testFindByIdAndFacilityId_Found() {
        // When
        Optional<Staff> result = staffRepository.findByIdAndFacilityId(activeStaff1.getId(), 1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(activeStaff1.getId());
        assertThat(result.get().getFacilityId()).isEqualTo(1L);
        assertThat(result.get().getEmail()).isEqualTo("active1@example.com");
    }

    @Test
    @DisplayName("Should return empty when staff ID exists but facility ID does not match")
    void testFindByIdAndFacilityId_WrongFacility() {
        // When
        Optional<Staff> result = staffRepository.findByIdAndFacilityId(activeStaff1.getId(), 2L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when staff ID does not exist")
    void testFindByIdAndFacilityId_NotFound() {
        // When
        Optional<Staff> result = staffRepository.findByIdAndFacilityId(999L, 1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all active staff across all facilities")
    void testFindByEmploymentStatus_Active() {
        // When
        List<Staff> result = staffRepository.findByEmploymentStatus("ACTIVE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Staff::getEmail)
                .containsExactlyInAnyOrder("active1@example.com", "active2@example.com", "facility2@example.com");
        assertThat(result).allMatch(staff -> staff.getEmploymentStatus().equals("ACTIVE"));
    }

    @Test
    @DisplayName("Should find all inactive staff across all facilities")
    void testFindByEmploymentStatus_Inactive() {
        // When
        List<Staff> result = staffRepository.findByEmploymentStatus("INACTIVE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("inactive@example.com");
        assertThat(result.get(0).getEmploymentStatus()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("Should return empty list when no staff has the specified employment status")
    void testFindByEmploymentStatus_NoMatch() {
        // When
        List<Staff> result = staffRepository.findByEmploymentStatus("TERMINATED");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return true when email exists for a different staff member")
    void testExistsByEmailAndIdNot_EmailExists() {
        // When
        boolean result = staffRepository.existsByEmailAndIdNot("active2@example.com", activeStaff1.getId());

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when email belongs to the same staff member")
    void testExistsByEmailAndIdNot_SameStaff() {
        // When
        boolean result = staffRepository.existsByEmailAndIdNot("active1@example.com", activeStaff1.getId());

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void testExistsByEmailAndIdNot_EmailDoesNotExist() {
        // When
        boolean result = staffRepository.existsByEmailAndIdNot("nonexistent@example.com", activeStaff1.getId());

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true when checking email uniqueness for new staff (ID is null)")
    void testExistsByEmailAndIdNot_NewStaffWithExistingEmail() {
        // When
        boolean result = staffRepository.existsByEmailAndIdNot("active1@example.com", null);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should find staff by email")
    void testFindByEmail_Found() {
        // When
        Optional<Staff> result = staffRepository.findByEmail("active1@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(activeStaff1.getId());
        assertThat(result.get().getEmail()).isEqualTo("active1@example.com");
        assertThat(result.get().getFirstName()).isEqualTo("John");
        assertThat(result.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void testFindByEmail_NotFound() {
        // When
        Optional<Staff> result = staffRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle null email in findByEmail")
    void testFindByEmail_NullEmail() {
        // When
        Optional<Staff> result = staffRepository.findByEmail(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle case-sensitive email search")
    void testFindByEmail_CaseSensitive() {
        // When
        Optional<Staff> result = staffRepository.findByEmail("ACTIVE1@EXAMPLE.COM");

        // Then
        // Assuming email is stored as lowercase
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should verify repository is annotated with @Repository")
    void testRepositoryAnnotation() {
        // Then
        assertThat(StaffRepository.class.isAnnotationPresent(Repository.class)).isTrue();
    }

    @Test
    @DisplayName("Should verify repository extends JpaRepository")
    void testRepositoryExtendsJpaRepository() {
        // Then
        assertThat(JpaRepository.class.isAssignableFrom(StaffRepository.class)).isTrue();
    }

    @Test
    @DisplayName("Should handle multiple staff with same employment status in different facilities")
    void testFindByEmploymentStatus_MultipleFacilities() {
        // Given - data already set up in @BeforeEach

        // When
        List<Staff> activeStaffList = staffRepository.findByEmploymentStatus("ACTIVE");

        // Then
        assertThat(activeStaffList).hasSize(3);
        assertThat(activeStaffList).extracting(Staff::getFacilityId)
                .containsExactlyInAnyOrder(1L, 1L, 2L);
    }

    @Test
    @DisplayName("Should handle empty string for employment status")
    void testFindByEmploymentStatus_EmptyString() {
        // When
        List<Staff> result = staffRepository.findByEmploymentStatus("");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle null facility ID in findByFacilityIdAndEmploymentStatus")
    void testFindByFacilityIdAndEmploymentStatus_NullFacilityId() {
        // When
        List<Staff> result = staffRepository.findByFacilityIdAndEmploymentStatus(null, "ACTIVE");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle null employment status in findByFacilityIdAndEmploymentStatus")
    void testFindByFacilityIdAndEmploymentStatus_NullStatus() {
        // When
        List<Staff> result = staffRepository.findByFacilityIdAndEmploymentStatus(1L, null);

        // Then
        assertThat(result).isEmpty();
    }
}