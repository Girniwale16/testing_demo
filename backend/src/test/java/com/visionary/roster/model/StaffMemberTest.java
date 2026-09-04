package com.visionary.roster.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StaffMember entity class.
 * Tests focus on the active field functionality and related methods.
 * 
 * @author Visionary Roster Team
 * @version 1.0
 */
public class StaffMemberTest {

    private StaffMember staffMember;

    @BeforeEach
    public void setUp() {
        staffMember = new StaffMember();
    }

    @Test
    @DisplayName("Test active field exists and has boolean type")
    public void testActiveFieldExistsAndIsBooleanType() {
        assertNotNull(staffMember);
        boolean active = staffMember.isActive();
        assertTrue(active instanceof Boolean);
    }

    @Test
    @DisplayName("Test active field default value is true")
    public void testActiveFieldDefaultValueIsTrue() {
        StaffMember newStaffMember = new StaffMember();
        assertTrue(newStaffMember.isActive());
    }

    @Test
    @DisplayName("Test active field default value is true with parameterized constructor")
    public void testActiveFieldDefaultValueIsTrueWithParameterizedConstructor() {
        StaffMember newStaffMember = new StaffMember("John", "Doe", "john.doe@example.com");
        assertTrue(newStaffMember.isActive());
    }

    @Test
    @DisplayName("Test isActive() getter method exists and returns boolean")
    public void testIsActiveGetterMethodExistsAndReturnsBoolean() {
        boolean result = staffMember.isActive();
        assertNotNull(result);
        assertTrue(result instanceof Boolean);
    }

    @Test
    @DisplayName("Test isActive() returns true when active is true")
    public void testIsActiveReturnsTrueWhenActiveIsTrue() {
        staffMember.setActive(true);
        assertTrue(staffMember.isActive());
    }

    @Test
    @DisplayName("Test isActive() returns false when active is false")
    public void testIsActiveReturnsFalseWhenActiveIsFalse() {
        staffMember.setActive(false);
        assertFalse(staffMember.isActive());
    }

    @Test
    @DisplayName("Test setActive(boolean) setter method exists")
    public void testSetActiveSetterMethodExists() {
        assertDoesNotThrow(() -> staffMember.setActive(true));
        assertDoesNotThrow(() -> staffMember.setActive(false));
    }

    @Test
    @DisplayName("Test setActive(true) sets active to true")
    public void testSetActiveTrueSetsActiveToTrue() {
        staffMember.setActive(false);
        staffMember.setActive(true);
        assertTrue(staffMember.isActive());
    }

    @Test
    @DisplayName("Test setActive(false) sets active to false")
    public void testSetActiveFalseSetsActiveToFalse() {
        staffMember.setActive(true);
        staffMember.setActive(false);
        assertFalse(staffMember.isActive());
    }

    @Test
    @DisplayName("Test active field can be toggled multiple times")
    public void testActiveFieldCanBeToggledMultipleTimes() {
        staffMember.setActive(true);
        assertTrue(staffMember.isActive());
        
        staffMember.setActive(false);
        assertFalse(staffMember.isActive());
        
        staffMember.setActive(true);
        assertTrue(staffMember.isActive());
        
        staffMember.setActive(false);
        assertFalse(staffMember.isActive());
    }

    @Test
    @DisplayName("Test active field is included in toString() method")
    public void testActiveFieldIsIncludedInToString() {
        staffMember.setActive(true);
        String toStringResult = staffMember.toString();
        assertTrue(toStringResult.contains("active=true"));
        
        staffMember.setActive(false);
        toStringResult = staffMember.toString();
        assertTrue(toStringResult.contains("active=false"));
    }

    @Test
    @DisplayName("Test StaffMember with all fields including active")
    public void testStaffMemberWithAllFieldsIncludingActive() {
        StaffMember member = new StaffMember("Jane", "Smith", "jane.smith@example.com");
        member.setId(1L);
        member.setActive(false);
        
        assertEquals(1L, member.getId());
        assertEquals("Jane", member.getFirstName());
        assertEquals("Smith", member.getLastName());
        assertEquals("jane.smith@example.com", member.getEmail());
        assertFalse(member.isActive());
    }

    @Test
    @DisplayName("Test active field persistence with default constructor")
    public void testActiveFieldPersistenceWithDefaultConstructor() {
        StaffMember member = new StaffMember();
        member.setFirstName("Test");
        member.setLastName("User");
        member.setEmail("test.user@example.com");
        member.setActive(false);
        
        assertFalse(member.isActive());
    }

    @Test
    @DisplayName("Test active field persistence with parameterized constructor")
    public void testActiveFieldPersistenceWithParameterizedConstructor() {
        StaffMember member = new StaffMember("Active", "Member", "active.member@example.com");
        assertTrue(member.isActive());
        
        member.setActive(false);
        assertFalse(member.isActive());
    }

    @Test
    @DisplayName("Test equals method is not affected by active field")
    public void testEqualsMethodNotAffectedByActiveField() {
        StaffMember member1 = new StaffMember("John", "Doe", "john@example.com");
        member1.setId(1L);
        member1.setActive(true);
        
        StaffMember member2 = new StaffMember("John", "Doe", "john@example.com");
        member2.setId(1L);
        member2.setActive(false);
        
        assertEquals(member1, member2);
    }

    @Test
    @DisplayName("Test hashCode method is not affected by active field")
    public void testHashCodeMethodNotAffectedByActiveField() {
        StaffMember member1 = new StaffMember("John", "Doe", "john@example.com");
        member1.setId(1L);
        member1.setActive(true);
        
        StaffMember member2 = new StaffMember("John", "Doe", "john@example.com");
        member2.setId(1L);
        member2.setActive(false);
        
        assertEquals(member1.hashCode(), member2.hashCode());
    }

    @Test
    @DisplayName("Test active field with null ID")
    public void testActiveFieldWithNullId() {
        StaffMember member = new StaffMember("Test", "Member", "test@example.com");
        assertNull(member.getId());
        assertTrue(member.isActive());
        
        member.setActive(false);
        assertFalse(member.isActive());
    }

    @Test
    @DisplayName("Test active field state is independent of other fields")
    public void testActiveFieldStateIsIndependentOfOtherFields() {
        staffMember.setFirstName("John");
        staffMember.setLastName("Doe");
        staffMember.setEmail("john.doe@example.com");
        staffMember.setActive(false);
        
        assertEquals("John", staffMember.getFirstName());
        assertEquals("Doe", staffMember.getLastName());
        assertEquals("john.doe@example.com", staffMember.getEmail());
        assertFalse(staffMember.isActive());
        
        staffMember.setFirstName("Jane");
        assertFalse(staffMember.isActive());
    }
}