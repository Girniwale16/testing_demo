package com.visionary.roster.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Facility Entity Tests - Staff Relationship")
class FacilityTest {

    private Facility facility;

    @Mock
    private Staff mockStaff1;

    @Mock
    private Staff mockStaff2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        facility = new Facility();
        facility.setFacilityId(1L);
        facility.setName("Test Facility");
        facility.setTimezone("America/New_York");
        facility.setRegionCode("US-EAST");
        facility.setIsActive(true);
        facility.setCreatedBy("test_user");
    }

    @Test
    @DisplayName("Should initialize staffMembers as empty HashSet")
    void testStaffMembersInitialization() {
        Facility newFacility = new Facility();
        assertNotNull(newFacility.getStaffMembers());
        assertTrue(newFacility.getStaffMembers().isEmpty());
    }

    @Test
    @DisplayName("Should return unmodifiable set from getStaffMembers")
    void testGetStaffMembersReturnsUnmodifiableSet() {
        Set<Staff> staffMembers = facility.getStaffMembers();
        assertNotNull(staffMembers);
        assertThrows(UnsupportedOperationException.class, () -> {
            staffMembers.add(mockStaff1);
        });
    }

    @Test
    @DisplayName("Should add staff and set bidirectional relationship")
    void testAddStaff() {
        facility.addStaff(mockStaff1);

        Set<Staff> staffMembers = facility.getStaffMembers();
        assertEquals(1, staffMembers.size());
        assertTrue(staffMembers.contains(mockStaff1));
        verify(mockStaff1, times(1)).setFacility(facility);
    }

    @Test
    @DisplayName("Should add multiple staff members")
    void testAddMultipleStaff() {
        facility.addStaff(mockStaff1);
        facility.addStaff(mockStaff2);

        Set<Staff> staffMembers = facility.getStaffMembers();
        assertEquals(2, staffMembers.size());
        assertTrue(staffMembers.contains(mockStaff1));
        assertTrue(staffMembers.contains(mockStaff2));
        verify(mockStaff1, times(1)).setFacility(facility);
        verify(mockStaff2, times(1)).setFacility(facility);
    }

    @Test
    @DisplayName("Should not add duplicate staff member")
    void testAddDuplicateStaff() {
        facility.addStaff(mockStaff1);
        facility.addStaff(mockStaff1);

        Set<Staff> staffMembers = facility.getStaffMembers();
        assertEquals(1, staffMembers.size());
        verify(mockStaff1, times(2)).setFacility(facility);
    }

    @Test
    @DisplayName("Should remove staff and clear bidirectional relationship")
    void testRemoveStaff() {
        facility.addStaff(mockStaff1);
        facility.removeStaff(mockStaff1);

        Set<Staff> staffMembers = facility.getStaffMembers();
        assertEquals(0, staffMembers.size());
        assertFalse(staffMembers.contains(mockStaff1));
        verify(mockStaff1, times(1)).setFacility(facility);
        verify(mockStaff1, times(1)).setFacility(null);
    }

    @Test
    @DisplayName("Should remove specific staff from multiple staff members")
    void testRemoveSpecificStaff() {
        facility.addStaff(mockStaff1);
        facility.addStaff(mockStaff2);
        facility.removeStaff(mockStaff1);

        Set<Staff> staffMembers = facility.getStaffMembers();
        assertEquals(1, staffMembers.size());
        assertFalse(staffMembers.contains(mockStaff1));
        assertTrue(staffMembers.contains(mockStaff2));
        verify(mockStaff1, times(1)).setFacility(null);
        verify(mockStaff2, never()).setFacility(null);
    }

    @Test
    @DisplayName("Should handle removing non-existent staff gracefully")
    void testRemoveNonExistentStaff() {
        facility.addStaff(mockStaff1);
        facility.removeStaff(mockStaff2);

        Set<Staff> staffMembers = facility.getStaffMembers();
        assertEquals(1, staffMembers.size());
        assertTrue(staffMembers.contains(mockStaff1));
        verify(mockStaff2, times(1)).setFacility(null);
    }

    @Test
    @DisplayName("Should maintain bidirectional consistency when adding staff")
    void testBidirectionalConsistencyOnAdd() {
        facility.addStaff(mockStaff1);

        verify(mockStaff1, times(1)).setFacility(facility);
        assertTrue(facility.getStaffMembers().contains(mockStaff1));
    }

    @Test
    @DisplayName("Should maintain bidirectional consistency when removing staff")
    void testBidirectionalConsistencyOnRemove() {
        facility.addStaff(mockStaff1);
        reset(mockStaff1);
        facility.removeStaff(mockStaff1);

        verify(mockStaff1, times(1)).setFacility(null);
        assertFalse(facility.getStaffMembers().contains(mockStaff1));
    }

    @Test
    @DisplayName("Should verify OneToMany annotation configuration")
    void testOneToManyAnnotationConfiguration() throws NoSuchFieldException {
        var field = Facility.class.getDeclaredField("staffMembers");
        OneToMany annotation = field.getAnnotation(OneToMany.class);

        assertNotNull(annotation);
        assertEquals("facility", annotation.mappedBy());
        assertArrayEquals(new CascadeType[]{CascadeType.ALL}, annotation.cascade());
        assertFalse(annotation.orphanRemoval());
    }

    @Test
    @DisplayName("Should verify orphanRemoval is false to prevent accidental deletion")
    void testOrphanRemovalIsFalse() throws NoSuchFieldException {
        var field = Facility.class.getDeclaredField("staffMembers");
        OneToMany annotation = field.getAnnotation(OneToMany.class);

        assertFalse(annotation.orphanRemoval(), "orphanRemoval should be false to prevent accidental staff deletion");
    }

    @Test
    @DisplayName("Should handle null staff in addStaff")
    void testAddNullStaff() {
        assertDoesNotThrow(() -> facility.addStaff(null));
    }

    @Test
    @DisplayName("Should handle null staff in removeStaff")
    void testRemoveNullStaff() {
        assertDoesNotThrow(() -> facility.removeStaff(null));
    }

    @Test
    @DisplayName("Should preserve existing staff when adding new staff")
    void testPreserveExistingStaffOnAdd() {
        facility.addStaff(mockStaff1);
        Set<Staff> firstSnapshot = Set.copyOf(facility.getStaffMembers());

        facility.addStaff(mockStaff2);
        Set<Staff> secondSnapshot = facility.getStaffMembers();

        assertTrue(secondSnapshot.contains(mockStaff1));
        assertTrue(secondSnapshot.contains(mockStaff2));
        assertEquals(2, secondSnapshot.size());
    }

    @Test
    @DisplayName("Should preserve remaining staff when removing one staff")
    void testPreserveRemainingStaffOnRemove() {
        facility.addStaff(mockStaff1);
        facility.addStaff(mockStaff2);

        facility.removeStaff(mockStaff1);
        Set<Staff> remainingStaff = facility.getStaffMembers();

        assertFalse(remainingStaff.contains(mockStaff1));
        assertTrue(remainingStaff.contains(mockStaff2));
        assertEquals(1, remainingStaff.size());
    }

    @Test
    @DisplayName("Should return empty set when no staff members exist")
    void testGetStaffMembersWhenEmpty() {
        Set<Staff> staffMembers = facility.getStaffMembers();

        assertNotNull(staffMembers);
        assertTrue(staffMembers.isEmpty());
        assertEquals(0, staffMembers.size());
    }

    @Test
    @DisplayName("Should maintain Set semantics preventing duplicates")
    void testSetSemanticsPreventsActualDuplicates() {
        Staff realStaff = new Staff();
        realStaff.setStaffId(1L);

        Facility realFacility = new Facility();
        realFacility.addStaff(realStaff);
        realFacility.addStaff(realStaff);

        assertEquals(1, realFacility.getStaffMembers().size());
    }

    @Test
    @DisplayName("Should verify cascade type is ALL")
    void testCascadeTypeIsAll() throws NoSuchFieldException {
        var field = Facility.class.getDeclaredField("staffMembers");
        OneToMany annotation = field.getAnnotation(OneToMany.class);

        CascadeType[] cascadeTypes = annotation.cascade();
        assertEquals(1, cascadeTypes.length);
        assertEquals(CascadeType.ALL, cascadeTypes[0]);
    }

    @Test
    @DisplayName("Should verify mappedBy is set to facility")
    void testMappedByIsFacility() throws NoSuchFieldException {
        var field = Facility.class.getDeclaredField("staffMembers");
        OneToMany annotation = field.getAnnotation(OneToMany.class);

        assertEquals("facility", annotation.mappedBy());
    }
}