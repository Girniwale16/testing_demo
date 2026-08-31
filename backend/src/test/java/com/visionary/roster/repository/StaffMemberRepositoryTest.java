package com.visionary.roster.repository;

import com.visionary.roster.entity.StaffMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for StaffMemberRepository.
 * Ensures 100% coverage of all repository methods and facility-scoped query patterns.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("StaffMemberRepository Tests")
class StaffMemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    private StaffMember staffMember1;
    private StaffMember staffMember2;
    private StaffMember staffMember3;
    private StaffMember staffMember4;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        staffMemberRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test data for facility 1
        staffMember1 = new StaffMember();
        staffMember1.setFacilityId(1L);
        staffMember1.setName("John Doe");
        staffMember1.setEmploymentStatus("ACTIVE");
        staffMember1.setStartDate(LocalDate.of(2020, 1, 1));
        staffMember1 = entityManager.persistAndFlush(staffMember1);

        staffMember2 = new StaffMember();
        staffMember2.setFacilityId(1L);
        staffMember2.setName("Jane Smith");
        staffMember2.setEmploymentStatus("INACTIVE");
        staffMember2.setStartDate(LocalDate.of(2019, 6, 15));
        staffMember2 = entityManager.persistAndFlush(staffMember2);

        // Create test data for facility 2
        staffMember3 = new StaffMember();
        staffMember3.setFacilityId(2L);
        staffMember3.setName("Bob Johnson");
        staffMember3.setEmploymentStatus("ACTIVE");
        staffMember3.setStartDate(LocalDate.of(2021, 3, 10));
        staffMember3 = entityManager.persistAndFlush(staffMember3);

        // Create another staff member for facility 1 with same name as staffMember1
        staffMember4 = new StaffMember();
        staffMember4.setFacilityId(1L);
        staffMember4.setName("John Doe");
        staffMember4.setEmploymentStatus("TERMINATED");
        staffMember4.setStartDate(LocalDate.of(2018, 1, 1));
        staffMember4.setEndDate(LocalDate.of(2019, 12, 31));
        staffMember4 = entityManager.persistAndFlush(staffMember4);

        entityManager.clear();
    }

    @Test
    @DisplayName("findById should return staff member when ID exists")
    void testFindById_WhenIdExists_ReturnsStaffMember() {
        // When
        Optional<StaffMember> result = staffMemberRepository.findById(staffMember1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(staffMember1.getId());
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getFacilityId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById should return empty Optional when ID does not exist")
    void testFindById_WhenIdDoesNotExist_ReturnsEmpty() {
        // When
        Optional<StaffMember> result = staffMemberRepository.findById(99999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById should return empty Optional when ID is null")
    void testFindById_WhenIdIsNull_ReturnsEmpty() {
        // When
        Optional<StaffMember> result = staffMemberRepository.findById(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFacilityId should return all staff members for given facility")
    void testFindByFacilityId_ReturnsAllStaffForFacility() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityId(1L);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(StaffMember::getFacilityId).containsOnly(1L);
        assertThat(result).extracting(StaffMember::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith", "John Doe");
    }

    @Test
    @DisplayName("findByFacilityId should return empty list when no staff members exist for facility")
    void testFindByFacilityId_WhenNoStaffExists_ReturnsEmptyList() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFacilityId should not return staff from other facilities")
    void testFindByFacilityId_DoesNotReturnStaffFromOtherFacilities() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityId(2L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Bob Johnson");
        assertThat(result.get(0).getFacilityId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should return filtered staff members")
    void testFindByFacilityIdAndEmploymentStatus_ReturnsFilteredStaff() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityIdAndEmploymentStatus(1L, "ACTIVE");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(0).getEmploymentStatus()).isEqualTo("ACTIVE");
        assertThat(result.get(0).getFacilityId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should return multiple staff with same status")
    void testFindByFacilityIdAndEmploymentStatus_ReturnsMultipleStaff() {
        // Given - create another active staff member
        StaffMember activeStaff = new StaffMember();
        activeStaff.setFacilityId(1L);
        activeStaff.setName("Alice Brown");
        activeStaff.setEmploymentStatus("ACTIVE");
        activeStaff.setStartDate(LocalDate.of(2022, 1, 1));
        entityManager.persistAndFlush(activeStaff);
        entityManager.clear();

        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityIdAndEmploymentStatus(1L, "ACTIVE");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(StaffMember::getEmploymentStatus).containsOnly("ACTIVE");
        assertThat(result).extracting(StaffMember::getFacilityId).containsOnly(1L);
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should return empty list when no matches")
    void testFindByFacilityIdAndEmploymentStatus_WhenNoMatches_ReturnsEmptyList() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityIdAndEmploymentStatus(1L, "ON_LEAVE");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should handle TERMINATED status")
    void testFindByFacilityIdAndEmploymentStatus_WithTerminatedStatus() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityIdAndEmploymentStatus(1L, "TERMINATED");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmploymentStatus()).isEqualTo("TERMINATED");
        assertThat(result.get(0).getEndDate()).isNotNull();
    }

    @Test
    @DisplayName("findByFacilityIdAndEmploymentStatus should not return staff from other facilities")
    void testFindByFacilityIdAndEmploymentStatus_DoesNotReturnOtherFacilities() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityIdAndEmploymentStatus(2L, "ACTIVE");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Bob Johnson");
        assertThat(result.get(0).getFacilityId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findByFacilityIdAndName should return staff members matching name")
    void testFindByFacilityIdAndName_ReturnsMatchingStaff() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityIdAndName(1L, "Jane Smith");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jane Smith");
        assertThat(result.get(0).getFacilityId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByFacilityIdAndName should return multiple staff with same name")
    void testFindByFacilityIdAndName_ReturnsMultipleStaffWithSameName() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityIdAndName(1L, "John Doe");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(StaffMember::getName).containsOnly("John Doe");
        assertThat(result).extracting(StaffMember::getFacilityId).containsOnly(1L);
    }

    @Test
    @DisplayName("findByFacilityIdAndName should return empty list when name does not exist")
    void testFindByFacilityIdAndName_WhenNameDoesNotExist_ReturnsEmptyList() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityIdAndName(1L, "Nonexistent Person");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFacilityIdAndName should not return staff from other facilities")
    void testFindByFacilityIdAndName_DoesNotReturnOtherFacilities() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityIdAndName(2L, "John Doe");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFacilityIdAndName should be case-sensitive")
    void testFindByFacilityIdAndName_IsCaseSensitive() {
        // When
        List<StaffMember> result = staffMemberRepository.findByFacilityIdAndName(1L, "john doe");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save should persist new staff member")
    void testSave_PersistsNewStaffMember() {
        // Given
        StaffMember newStaff = new StaffMember();
        newStaff.setFacilityId(3L);
        newStaff.setName("New Staff");
        newStaff.setEmploymentStatus("ACTIVE");
        newStaff.setStartDate(LocalDate.now());

        // When
        StaffMember saved = staffMemberRepository.save(newStaff);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(saved.getId()).isNotNull();
        Optional<StaffMember> retrieved = staffMemberRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("New Staff");
        assertThat(retrieved.get().getFacilityId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("save should update existing staff member")
    void testSave_UpdatesExistingStaffMember() {
        // Given
        StaffMember existing = staffMemberRepository.findById(staffMember1.getId()).orElseThrow();
        existing.setEmploymentStatus("INACTIVE");
        existing.setEndDate(LocalDate.now());

        // When
        StaffMember updated = staffMemberRepository.save(existing);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<StaffMember> retrieved = staffMemberRepository.findById(staffMember1.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getEmploymentStatus()).isEqualTo("INACTIVE");
        assertThat(retrieved.get().getEndDate()).isNotNull();
    }

    @Test
    @DisplayName("save should handle soft-delete pattern via status update")
    void testSave_HandlesSoftDeletePattern() {
        // Given
        StaffMember toSoftDelete = staffMemberRepository.findById(staffMember1.getId()).orElseThrow();
        toSoftDelete.setEmploymentStatus("TERMINATED");
        toSoftDelete.setEndDate(LocalDate.now());

        // When
        staffMemberRepository.save(toSoftDelete);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<StaffMember> retrieved = staffMemberRepository.findById(staffMember1.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getEmploymentStatus()).isEqualTo("TERMINATED");
        assertThat(retrieved.get().getEndDate()).isNotNull();
        
        // Verify soft-deleted staff is not in active queries
        List<StaffMember> activeStaff = staffMemberRepository.findByFacilityIdAndEmploymentStatus(1L, "ACTIVE");
        assertThat(activeStaff).doesNotContain(retrieved.get());
    }

    @Test
    @DisplayName("deleteById should physically remove staff member")
    void testDeleteById_RemovesStaffMember() {
        // Given
        Long idToDelete = staffMember1.getId();

        // When
        staffMemberRepository.deleteById(idToDelete);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<StaffMember> result = staffMemberRepository.findById(idToDelete);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteById should not affect other staff members")
    void testDeleteById_DoesNotAffectOtherStaff() {
        // Given
        Long idToDelete = staffMember1.getId();
        int initialCount = staffMemberRepository.findAll().size();

        // When
        staffMemberRepository.deleteById(idToDelete);
        entityManager.flush();
        entityManager.clear();

        // Then
        List<StaffMember> remaining = staffMemberRepository.findAll();
        assertThat(remaining).hasSize(initialCount - 1);
        assertThat(remaining).extracting(StaffMember::getId).doesNotContain(idToDelete);
    }

    @Test
    @DisplayName("Repository should maintain facility isolation across all queries")
    void testFacilityIsolation_AcrossAllQueries() {
        // When - Query facility 1
        List<StaffMember> facility1All = staffMemberRepository.findByFacilityId(1L);
        List<StaffMember> facility1Active = staffMemberRepository.findByFacilityIdAndEmploymentStatus(1L, "ACTIVE");
        List<StaffMember> facility1ByName = staffMemberRepository.findByFacilityIdAndName(1L, "John Doe");

        // Then - All queries should only return facility 1 data
        assertThat(facility1All).extracting(StaffMember::getFacilityId).containsOnly(1L);
        assertThat(facility1Active).extracting(StaffMember::getFacilityId).containsOnly(1L);
        assertThat(facility1ByName).extracting(StaffMember::getFacilityId).containsOnly(1L);

        // When - Query facility 2
        List<StaffMember> facility2All = staffMemberRepository.findByFacilityId(2L);

        // Then - Should not contain any facility 1 data
        assertThat(facility2All).extracting(StaffMember::getFacilityId).containsOnly(2L);
        assertThat(facility2All).hasSize(1);
    }

    @Test
    @DisplayName("Repository should handle composite index queries efficiently")
    void testCompositeIndexQueries_ExecuteSuccessfully() {
        // This test verifies that composite index queries execute without errors
        // Actual performance testing would require integration tests with real database

        // When - Execute all composite index queries
        List<StaffMember> byFacility = staffMemberRepository.findByFacilityId(1L);
        List<StaffMember> byFacilityAndStatus = staffMemberRepository.findByFacilityIdAndEmploymentStatus(1L, "ACTIVE");
        List<StaffMember> byFacilityAndName = staffMemberRepository.findByFacilityIdAndName(1L, "John Doe");

        // Then - All queries should complete successfully
        assertThat(byFacility).isNotNull();
        assertThat(byFacilityAndStatus).isNotNull();
        assertThat(byFacilityAndName).isNotNull();
    }

    @Test
    @DisplayName("Repository should support multiple employment statuses")
    void testMultipleEmploymentStatuses() {
        // When
        List<StaffMember> active = staffMemberRepository.findByFacilityIdAndEmploymentStatus(1L, "ACTIVE");
        List<StaffMember> inactive = staffMemberRepository.findByFacilityIdAndEmploymentStatus(1L, "INACTIVE");
        List<StaffMember> terminated = staffMemberRepository.findByFacilityIdAndEmploymentStatus(1L, "TERMINATED");

        // Then
        assertThat(active).hasSize(1);
        assertThat(inactive).hasSize(1);
        assertThat(terminated).hasSize(1);
        
        int totalForFacility = active.size() + inactive.size() + terminated.size();
        List<StaffMember> allForFacility = staffMemberRepository.findByFacilityId(1L);
        assertThat(allForFacility).hasSize(totalForFacility);
    }
}