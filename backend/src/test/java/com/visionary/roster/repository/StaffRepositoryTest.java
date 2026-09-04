package com.visionary.roster.repository;

import com.visionary.roster.model.Facility;
import com.visionary.roster.model.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StaffRepository.
 * Tests all repository methods including newly added active status filtering.
 */
@DataJpaTest
@ActiveProfiles("test")
class StaffRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StaffRepository staffRepository;

    private Facility testFacility;
    private Staff activeStaff1;
    private Staff activeStaff2;
    private Staff inactiveStaff1;
    private Staff inactiveStaff2;

    @BeforeEach
    void setUp() {
        // Create test facility
        testFacility = new Facility();
        testFacility.setFacilityName("Test Hospital");
        testFacility.setAddress("123 Test St");
        testFacility.setCity("Test City");
        testFacility.setState("TS");
        testFacility.setZipCode("12345");
        testFacility = entityManager.persist(testFacility);

        // Create active staff members
        activeStaff1 = new Staff();
        activeStaff1.setFirstName("John");
        activeStaff1.setLastName("Doe");
        activeStaff1.setEmail("john.doe@test.com");
        activeStaff1.setPhoneNumber("1234567890");
        activeStaff1.setRole("NURSE");
        activeStaff1.setEmploymentStatus("ACTIVE");
        activeStaff1.setActive(true);
        activeStaff1.setFacility(testFacility);
        activeStaff1 = entityManager.persist(activeStaff1);

        activeStaff2 = new Staff();
        activeStaff2.setFirstName("Jane");
        activeStaff2.setLastName("Smith");
        activeStaff2.setEmail("jane.smith@test.com");
        activeStaff2.setPhoneNumber("0987654321");
        activeStaff2.setRole("DOCTOR");
        activeStaff2.setEmploymentStatus("ACTIVE");
        activeStaff2.setActive(true);
        activeStaff2.setFacility(testFacility);
        activeStaff2 = entityManager.persist(activeStaff2);

        // Create inactive staff members
        inactiveStaff1 = new Staff();
        inactiveStaff1.setFirstName("Bob");
        inactiveStaff1.setLastName("Johnson");
        inactiveStaff1.setEmail("bob.johnson@test.com");
        inactiveStaff1.setPhoneNumber("5555555555");
        inactiveStaff1.setRole("NURSE");
        inactiveStaff1.setEmploymentStatus("INACTIVE");
        inactiveStaff1.setActive(false);
        inactiveStaff1.setFacility(testFacility);
        inactiveStaff1 = entityManager.persist(inactiveStaff1);

        inactiveStaff2 = new Staff();
        inactiveStaff2.setFirstName("Alice");
        inactiveStaff2.setLastName("Williams");
        inactiveStaff2.setEmail("alice.williams@test.com");
        inactiveStaff2.setPhoneNumber("6666666666");
        inactiveStaff2.setRole("ADMIN");
        inactiveStaff2.setEmploymentStatus("ACTIVE");
        inactiveStaff2.setActive(false);
        inactiveStaff2.setFacility(testFacility);
        inactiveStaff2 = entityManager.persist(inactiveStaff2);

        entityManager.flush();
    }

    @Test
    void testFindByActive_WhenActiveIsTrue_ShouldReturnOnlyActiveStaff() {
        // When
        List<Staff> activeStaffList = staffRepository.findByActive(true);

        // Then
        assertThat(activeStaffList).isNotNull();
        assertThat(activeStaffList).hasSize(2);
        assertThat(activeStaffList).extracting(Staff::getActive).containsOnly(true);
        assertThat(activeStaffList).extracting(Staff::getEmail)
                .containsExactlyInAnyOrder("john.doe@test.com", "jane.smith@test.com");
    }

    @Test
    void testFindByActive_WhenActiveIsFalse_ShouldReturnOnlyInactiveStaff() {
        // When
        List<Staff> inactiveStaffList = staffRepository.findByActive(false);

        // Then
        assertThat(inactiveStaffList).isNotNull();
        assertThat(inactiveStaffList).hasSize(2);
        assertThat(inactiveStaffList).extracting(Staff::getActive).containsOnly(false);
        assertThat(inactiveStaffList).extracting(Staff::getEmail)
                .containsExactlyInAnyOrder("bob.johnson@test.com", "alice.williams@test.com");
    }

    @Test
    void testFindByActive_WhenNoActiveStaff_ShouldReturnEmptyList() {
        // Given - deactivate all staff
        activeStaff1.setActive(false);
        activeStaff2.setActive(false);
        entityManager.persist(activeStaff1);
        entityManager.persist(activeStaff2);
        entityManager.flush();

        // When
        List<Staff> activeStaffList = staffRepository.findByActive(true);

        // Then
        assertThat(activeStaffList).isNotNull();
        assertThat(activeStaffList).isEmpty();
    }

    @Test
    void testFindByFacilityIdAndActive_WhenActiveIsTrue_ShouldReturnOnlyActiveStaffForFacility() {
        // When
        List<Staff> activeStaffList = staffRepository.findByFacilityIdAndActive(testFacility.getFacilityId(), true);

        // Then
        assertThat(activeStaffList).isNotNull();
        assertThat(activeStaffList).hasSize(2);
        assertThat(activeStaffList).extracting(Staff::getActive).containsOnly(true);
        assertThat(activeStaffList).extracting(Staff::getFacility).extracting(Facility::getFacilityId)
                .containsOnly(testFacility.getFacilityId());
        assertThat(activeStaffList).extracting(Staff::getEmail)
                .containsExactlyInAnyOrder("john.doe@test.com", "jane.smith@test.com");
    }

    @Test
    void testFindByFacilityIdAndActive_WhenActiveIsFalse_ShouldReturnOnlyInactiveStaffForFacility() {
        // When
        List<Staff> inactiveStaffList = staffRepository.findByFacilityIdAndActive(testFacility.getFacilityId(), false);

        // Then
        assertThat(inactiveStaffList).isNotNull();
        assertThat(inactiveStaffList).hasSize(2);
        assertThat(inactiveStaffList).extracting(Staff::getActive).containsOnly(false);
        assertThat(inactiveStaffList).extracting(Staff::getFacility).extracting(Facility::getFacilityId)
                .containsOnly(testFacility.getFacilityId());
        assertThat(inactiveStaffList).extracting(Staff::getEmail)
                .containsExactlyInAnyOrder("bob.johnson@test.com", "alice.williams@test.com");
    }

    @Test
    void testFindByFacilityIdAndActive_WhenFacilityHasNoActiveStaff_ShouldReturnEmptyList() {
        // Given - create another facility with no active staff
        Facility anotherFacility = new Facility();
        anotherFacility.setFacilityName("Another Hospital");
        anotherFacility.setAddress("456 Another St");
        anotherFacility.setCity("Another City");
        anotherFacility.setState("AC");
        anotherFacility.setZipCode("54321");
        anotherFacility = entityManager.persist(anotherFacility);
        entityManager.flush();

        // When
        List<Staff> activeStaffList = staffRepository.findByFacilityIdAndActive(anotherFacility.getFacilityId(), true);

        // Then
        assertThat(activeStaffList).isNotNull();
        assertThat(activeStaffList).isEmpty();
    }

    @Test
    void testFindByFacilityIdAndActive_WhenInvalidFacilityId_ShouldReturnEmptyList() {
        // When
        List<Staff> staffList = staffRepository.findByFacilityIdAndActive(99999L, true);

        // Then
        assertThat(staffList).isNotNull();
        assertThat(staffList).isEmpty();
    }

    @Test
    void testDeactivationWorkflow_StaffWithActiveFalse_ShouldBeExcludedFromActiveQueries() {
        // Given - verify staff is initially active
        List<Staff> initialActiveStaff = staffRepository.findByActive(true);
        assertThat(initialActiveStaff).hasSize(2);
        assertThat(initialActiveStaff).extracting(Staff::getEmail).contains("john.doe@test.com");

        // When - deactivate staff using save method
        activeStaff1.setActive(false);
        staffRepository.save(activeStaff1);
        entityManager.flush();
        entityManager.clear();

        // Then - verify deactivated staff is excluded from active queries
        List<Staff> activeStaffAfterDeactivation = staffRepository.findByActive(true);
        assertThat(activeStaffAfterDeactivation).hasSize(1);
        assertThat(activeStaffAfterDeactivation).extracting(Staff::getEmail)
                .doesNotContain("john.doe@test.com")
                .contains("jane.smith@test.com");

        // And - verify deactivated staff appears in inactive queries
        List<Staff> inactiveStaffAfterDeactivation = staffRepository.findByActive(false);
        assertThat(inactiveStaffAfterDeactivation).hasSize(3);
        assertThat(inactiveStaffAfterDeactivation).extracting(Staff::getEmail)
                .contains("john.doe@test.com");
    }

    @Test
    void testDeactivationWorkflow_FacilityScopedQuery_ShouldExcludeDeactivatedStaff() {
        // Given - verify initial state
        List<Staff> initialActiveStaff = staffRepository.findByFacilityIdAndActive(testFacility.getFacilityId(), true);
        assertThat(initialActiveStaff).hasSize(2);

        // When - deactivate staff
        activeStaff1.setActive(false);
        activeStaff2.setActive(false);
        staffRepository.save(activeStaff1);
        staffRepository.save(activeStaff2);
        entityManager.flush();
        entityManager.clear();

        // Then - verify no active staff for facility
        List<Staff> activeStaffAfterDeactivation = staffRepository.findByFacilityIdAndActive(testFacility.getFacilityId(), true);
        assertThat(activeStaffAfterDeactivation).isEmpty();

        // And - verify all staff appear in inactive query
        List<Staff> inactiveStaffAfterDeactivation = staffRepository.findByFacilityIdAndActive(testFacility.getFacilityId(), false);
        assertThat(inactiveStaffAfterDeactivation).hasSize(4);
    }

    @Test
    void testExistsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // When
        boolean exists = staffRepository.existsByEmail("john.doe@test.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = staffRepository.existsByEmail("nonexistent@test.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void testExistsByEmailAndIdNot_WhenEmailExistsForDifferentStaff_ShouldReturnTrue() {
        // When
        boolean exists = staffRepository.existsByEmailAndIdNot("john.doe@test.com", activeStaff2.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByEmailAndIdNot_WhenEmailExistsForSameStaff_ShouldReturnFalse() {
        // When
        boolean exists = staffRepository.existsByEmailAndIdNot("john.doe@test.com", activeStaff1.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void testFindByEmail_WhenEmailExists_ShouldReturnStaff() {
        // When
        Optional<Staff> foundStaff = staffRepository.findByEmail("john.doe@test.com");

        // Then
        assertThat(foundStaff).isPresent();
        assertThat(foundStaff.get().getFirstName()).isEqualTo("John");
        assertThat(foundStaff.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void testFindByEmail_WhenEmailDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Staff> foundStaff = staffRepository.findByEmail("nonexistent@test.com");

        // Then
        assertThat(foundStaff).isEmpty();
    }

    @Test
    void testFindByEmploymentStatus_ShouldReturnStaffWithMatchingStatus() {
        // When
        List<Staff> activeEmploymentStaff = staffRepository.findByEmploymentStatus("ACTIVE");

        // Then
        assertThat(activeEmploymentStaff).hasSize(3);
        assertThat(activeEmploymentStaff).extracting(Staff::getEmploymentStatus).containsOnly("ACTIVE");
    }

    @Test
    void testFindByFacilityId_ShouldReturnAllStaffForFacility() {
        // When
        List<Staff> facilityStaff = staffRepository.findByFacilityId(testFacility.getFacilityId());

        // Then
        assertThat(facilityStaff).hasSize(4);
        assertThat(facilityStaff).extracting(Staff::getFacility).extracting(Facility::getFacilityId)
                .containsOnly(testFacility.getFacilityId());
    }

    @Test
    void testFindByFacility_FacilityIdAndEmploymentStatus_ShouldReturnMatchingStaff() {
        // When
        List<Staff> activeStaffInFacility = staffRepository.findByFacility_FacilityIdAndEmploymentStatus(
                testFacility.getFacilityId(), "ACTIVE");

        // Then
        assertThat(activeStaffInFacility).hasSize(3);
        assertThat(activeStaffInFacility).extracting(Staff::getEmploymentStatus).containsOnly("ACTIVE");
        assertThat(activeStaffInFacility).extracting(Staff::getFacility).extracting(Facility::getFacilityId)
                .containsOnly(testFacility.getFacilityId());
    }
}