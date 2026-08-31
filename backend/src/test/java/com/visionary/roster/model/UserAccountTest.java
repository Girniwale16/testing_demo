package com.visionary.roster.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserAccount Entity Tests")
class UserAccountTest {

    private UserAccount userAccount;
    private Facility facility;

    @BeforeEach
    void setUp() {
        facility = new Facility();
        facility.setFacilityId(1L);
        facility.setFacilityName("Test Facility");

        userAccount = new UserAccount();
        userAccount.setUserAccountId(1L);
        userAccount.setFacility(facility);
        userAccount.setUsername("testuser");
        userAccount.setPasswordHash("hashedpassword123");
        userAccount.setRole("ADMIN");
        userAccount.setIsActive(true);
    }

    @Test
    @DisplayName("Test isAccountActive returns true when accountStatus is null")
    void testIsAccountActive_WhenAccountStatusIsNull_ReturnsTrue() {
        userAccount.setAccountStatus(null);
        
        assertTrue(userAccount.isAccountActive(), 
            "isAccountActive should return true when accountStatus is null");
    }

    @Test
    @DisplayName("Test isAccountActive returns true when accountStatus is ACTIVE")
    void testIsAccountActive_WhenAccountStatusIsActive_ReturnsTrue() {
        userAccount.setAccountStatus("ACTIVE");
        
        assertTrue(userAccount.isAccountActive(), 
            "isAccountActive should return true when accountStatus is ACTIVE");
    }

    @Test
    @DisplayName("Test isAccountActive returns false when accountStatus is INACTIVE")
    void testIsAccountActive_WhenAccountStatusIsInactive_ReturnsFalse() {
        userAccount.setAccountStatus("INACTIVE");
        
        assertFalse(userAccount.isAccountActive(), 
            "isAccountActive should return false when accountStatus is INACTIVE");
    }

    @Test
    @DisplayName("Test isAccountActive returns false when accountStatus is SUSPENDED")
    void testIsAccountActive_WhenAccountStatusIsSuspended_ReturnsFalse() {
        userAccount.setAccountStatus("SUSPENDED");
        
        assertFalse(userAccount.isAccountActive(), 
            "isAccountActive should return false when accountStatus is SUSPENDED");
    }

    @Test
    @DisplayName("Test isAccountActive returns false when accountStatus is LOCKED")
    void testIsAccountActive_WhenAccountStatusIsLocked_ReturnsFalse() {
        userAccount.setAccountStatus("LOCKED");
        
        assertFalse(userAccount.isAccountActive(), 
            "isAccountActive should return false when accountStatus is LOCKED");
    }

    @ParameterizedTest
    @ValueSource(strings = {"INACTIVE", "SUSPENDED", "LOCKED", "DISABLED", "TERMINATED", "EXPIRED"})
    @DisplayName("Test isAccountActive returns false for various non-active statuses")
    void testIsAccountActive_WithVariousNonActiveStatuses_ReturnsFalse(String status) {
        userAccount.setAccountStatus(status);
        
        assertFalse(userAccount.isAccountActive(), 
            "isAccountActive should return false for status: " + status);
    }

    @Test
    @DisplayName("Test isAccountActive is case-sensitive for ACTIVE status")
    void testIsAccountActive_IsCaseSensitive_ForActiveStatus() {
        userAccount.setAccountStatus("active");
        
        assertFalse(userAccount.isAccountActive(), 
            "isAccountActive should be case-sensitive and return false for lowercase 'active'");
        
        userAccount.setAccountStatus("Active");
        assertFalse(userAccount.isAccountActive(), 
            "isAccountActive should be case-sensitive and return false for 'Active'");
        
        userAccount.setAccountStatus("ACTIVE");
        assertTrue(userAccount.isAccountActive(), 
            "isAccountActive should return true only for uppercase 'ACTIVE'");
    }

    @Test
    @DisplayName("Test accountStatus field can be set and retrieved")
    void testAccountStatus_CanBeSetAndRetrieved() {
        userAccount.setAccountStatus("ACTIVE");
        assertEquals("ACTIVE", userAccount.getAccountStatus(), 
            "accountStatus should be retrievable after being set");
        
        userAccount.setAccountStatus("INACTIVE");
        assertEquals("INACTIVE", userAccount.getAccountStatus(), 
            "accountStatus should be updated correctly");
    }

    @Test
    @DisplayName("Test accountEndDate field can be set and retrieved")
    void testAccountEndDate_CanBeSetAndRetrieved() {
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        userAccount.setAccountEndDate(endDate);
        
        assertEquals(endDate, userAccount.getAccountEndDate(), 
            "accountEndDate should be retrievable after being set");
    }

    @Test
    @DisplayName("Test accountEndDate can be null")
    void testAccountEndDate_CanBeNull() {
        userAccount.setAccountEndDate(null);
        
        assertNull(userAccount.getAccountEndDate(), 
            "accountEndDate should be allowed to be null");
    }

    @Test
    @DisplayName("Test account deactivation scenario with status and end date")
    void testAccountDeactivation_WithStatusAndEndDate() {
        LocalDate endDate = LocalDate.now();
        userAccount.setAccountStatus("INACTIVE");
        userAccount.setAccountEndDate(endDate);
        
        assertFalse(userAccount.isAccountActive(), 
            "Account should be inactive when status is INACTIVE");
        assertEquals("INACTIVE", userAccount.getAccountStatus());
        assertEquals(endDate, userAccount.getAccountEndDate());
    }

    @Test
    @DisplayName("Test account activation scenario")
    void testAccountActivation_WithActiveStatus() {
        userAccount.setAccountStatus("ACTIVE");
        userAccount.setAccountEndDate(null);
        
        assertTrue(userAccount.isAccountActive(), 
            "Account should be active when status is ACTIVE");
        assertEquals("ACTIVE", userAccount.getAccountStatus());
        assertNull(userAccount.getAccountEndDate());
    }

    @Test
    @DisplayName("Test facilityId field exists through facility relationship")
    void testFacilityId_ExistsThroughFacilityRelationship() {
        assertNotNull(userAccount.getFacility(), 
            "Facility should be set on user account");
        assertEquals(1L, userAccount.getFacility().getFacilityId(), 
            "Facility ID should be accessible through facility relationship");
    }

    @Test
    @DisplayName("Test onCreate sets default values correctly")
    void testOnCreate_SetsDefaultValues() {
        UserAccount newAccount = new UserAccount();
        newAccount.onCreate();
        
        assertNotNull(newAccount.getCreatedAt(), 
            "createdAt should be set by onCreate");
        assertEquals("system", newAccount.getCreatedBy(), 
            "createdBy should default to 'system' when null");
        assertTrue(newAccount.getIsActive(), 
            "isActive should default to true when null");
    }

    @Test
    @DisplayName("Test onCreate preserves existing createdBy value")
    void testOnCreate_PreservesExistingCreatedBy() {
        UserAccount newAccount = new UserAccount();
        newAccount.setCreatedBy("admin");
        newAccount.onCreate();
        
        assertEquals("admin", newAccount.getCreatedBy(), 
            "createdBy should not be overwritten if already set");
    }

    @Test
    @DisplayName("Test onUpdate sets updatedAt timestamp")
    void testOnUpdate_SetsUpdatedAtTimestamp() {
        userAccount.onUpdate();
        
        assertNotNull(userAccount.getUpdatedAt(), 
            "updatedAt should be set by onUpdate");
    }

    @Test
    @DisplayName("Test distinction between account status and isActive flag")
    void testDistinction_BetweenAccountStatusAndIsActiveFlag() {
        userAccount.setIsActive(true);
        userAccount.setAccountStatus("INACTIVE");
        
        assertTrue(userAccount.getIsActive(), 
            "isActive flag can be true");
        assertFalse(userAccount.isAccountActive(), 
            "isAccountActive() should return false based on accountStatus");
    }

    @Test
    @DisplayName("Test account can have end date without being inactive")
    void testAccount_CanHaveEndDateWithoutBeingInactive() {
        LocalDate futureDate = LocalDate.now().plusDays(30);
        userAccount.setAccountStatus("ACTIVE");
        userAccount.setAccountEndDate(futureDate);
        
        assertTrue(userAccount.isAccountActive(), 
            "Account should be active even with future end date");
        assertEquals(futureDate, userAccount.getAccountEndDate());
    }

    @Test
    @DisplayName("Test account status field has correct length constraint")
    void testAccountStatus_HasCorrectLengthConstraint() {
        String longStatus = "A".repeat(20);
        userAccount.setAccountStatus(longStatus);
        
        assertEquals(20, userAccount.getAccountStatus().length(), 
            "accountStatus should support up to 20 characters");
    }

    @Test
    @DisplayName("Test empty string accountStatus is treated as non-active")
    void testAccountStatus_EmptyStringTreatedAsNonActive() {
        userAccount.setAccountStatus("");
        
        assertFalse(userAccount.isAccountActive(), 
            "Empty string accountStatus should be treated as non-active");
    }

    @Test
    @DisplayName("Test whitespace-only accountStatus is treated as non-active")
    void testAccountStatus_WhitespaceOnlyTreatedAsNonActive() {
        userAccount.setAccountStatus("   ");
        
        assertFalse(userAccount.isAccountActive(), 
            "Whitespace-only accountStatus should be treated as non-active");
    }

    @Test
    @DisplayName("Test account with null status and past end date")
    void testAccount_WithNullStatusAndPastEndDate() {
        LocalDate pastDate = LocalDate.now().minusDays(30);
        userAccount.setAccountStatus(null);
        userAccount.setAccountEndDate(pastDate);
        
        assertTrue(userAccount.isAccountActive(), 
            "isAccountActive only checks accountStatus, not accountEndDate");
    }

    @Test
    @DisplayName("Test staffMemberId can be null for non-staff users")
    void testStaffMemberId_CanBeNullForNonStaffUsers() {
        userAccount.setStaffMemberId(null);
        
        assertNull(userAccount.getStaffMemberId(), 
            "staffMemberId should be allowed to be null for non-staff users like contractors or auditors");
        assertTrue(userAccount.isAccountActive(), 
            "Account can be active without being associated with a staff member");
    }

    @Test
    @DisplayName("Test complete account lifecycle from creation to deactivation")
    void testCompleteAccountLifecycle_FromCreationToDeactivation() {
        UserAccount newAccount = new UserAccount();
        newAccount.setFacility(facility);
        newAccount.setUsername("lifecycleuser");
        newAccount.setPasswordHash("hash123");
        newAccount.setRole("USER");
        newAccount.onCreate();
        
        assertTrue(newAccount.isAccountActive(), 
            "New account should be active by default");
        assertNull(newAccount.getAccountStatus(), 
            "New account should have null accountStatus");
        
        newAccount.setAccountStatus("ACTIVE");
        assertTrue(newAccount.isAccountActive(), 
            "Account should remain active with ACTIVE status");
        
        newAccount.setAccountStatus("SUSPENDED");
        newAccount.onUpdate();
        assertFalse(newAccount.isAccountActive(), 
            "Account should be inactive when suspended");
        assertNotNull(newAccount.getUpdatedAt(), 
            "updatedAt should be set on status change");
        
        LocalDate endDate = LocalDate.now();
        newAccount.setAccountStatus("INACTIVE");
        newAccount.setAccountEndDate(endDate);
        assertFalse(newAccount.isAccountActive(), 
            "Account should be inactive with end date set");
        assertEquals(endDate, newAccount.getAccountEndDate());
    }
}