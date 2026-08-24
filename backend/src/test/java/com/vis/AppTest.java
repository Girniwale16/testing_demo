package com.vis;

import com.vis.model.Facility;
import com.vis.model.UserAccount;
import com.vis.repository.FacilityRepository;
import com.vis.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppTest {

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private Facility testFacility;

    @BeforeEach
    void setUp() {
        testFacility = new Facility();
        testFacility.setTimezone("America/New_York");
        testFacility.setRegionCode("US-EAST");
        testFacility.setIsActive(true);
        testFacility.setCreatedAt(LocalDateTime.now());
        testFacility = facilityRepository.save(testFacility);
    }

    @Test
    void saveFacility_validData_returnsPersistedEntity() {
        Facility facility = new Facility();
        facility.setTimezone("Europe/London");
        facility.setRegionCode("UK");
        facility.setIsActive(true);
        facility.setCreatedAt(LocalDateTime.now());

        Facility saved = facilityRepository.save(facility);

        assertNotNull(saved.getFacilityId());
        assertEquals("Europe/London", saved.getTimezone());
        assertEquals("UK", saved.getRegionCode());
        assertTrue(saved.getIsActive());
    }

    @Test
    void saveFacility_nullTimezone_throwsException() {
        Facility facility = new Facility();
        facility.setRegionCode("US-WEST");
        facility.setIsActive(true);
        facility.setCreatedAt(LocalDateTime.now());

        assertThrows(Exception.class, () -> {
            facilityRepository.save(facility);
            facilityRepository.flush();
        });
    }

    @Test
    void findFacilityById_existingId_returnsFacility() {
        Optional<Facility> found = facilityRepository.findById(testFacility.getFacilityId());

        assertTrue(found.isPresent());
        assertEquals("America/New_York", found.get().getTimezone());
    }

    @Test
    void findFacilityById_nonExistingId_returnsEmpty() {
        Optional<Facility> found = facilityRepository.findById(99999L);

        assertFalse(found.isPresent());
    }

    @Test
    void saveUserAccount_validData_returnsPersistedEntity() {
        UserAccount userAccount = new UserAccount();
        userAccount.setFacility(testFacility);
        userAccount.setUsername("testuser");
        userAccount.setPasswordHash("$2a$10$hashedpassword");
        userAccount.setRole(UserAccount.UserRole.MANAGER);
        userAccount.setCreatedAt(LocalDateTime.now());

        UserAccount saved = userAccountRepository.save(userAccount);

        assertNotNull(saved.getUserAccountId());
        assertEquals("testuser", saved.getUsername());
        assertEquals(UserAccount.UserRole.MANAGER, saved.getRole());
        assertEquals(testFacility.getFacilityId(), saved.getFacility().getFacilityId());
    }

    @Test
    void saveUserAccount_duplicateUsername_throwsException() {
        UserAccount userAccount1 = new UserAccount();
        userAccount1.setFacility(testFacility);
        userAccount1.setUsername("duplicate");
        userAccount1.setPasswordHash("$2a$10$hash1");
        userAccount1.setRole(UserAccount.UserRole.STAFF);
        userAccount1.setCreatedAt(LocalDateTime.now());
        userAccountRepository.save(userAccount1);

        UserAccount userAccount2 = new UserAccount();
        userAccount2.setFacility(testFacility);
        userAccount2.setUsername("duplicate");
        userAccount2.setPasswordHash("$2a$10$hash2");
        userAccount2.setRole(UserAccount.UserRole.MANAGER);
        userAccount2.setCreatedAt(LocalDateTime.now());

        assertThrows(Exception.class, () -> {
            userAccountRepository.save(userAccount2);
            userAccountRepository.flush();
        });
    }

    @Test
    void findUserAccountByFacilityAndUsername_existingUser_returnsUser() {
        UserAccount userAccount = new UserAccount();
        userAccount.setFacility(testFacility);
        userAccount.setUsername("findme");
        userAccount.setPasswordHash("$2a$10$hash");
        userAccount.setRole(UserAccount.UserRole.SUPERVISOR);
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccountRepository.save(userAccount);

        Optional<UserAccount> found = userAccountRepository.findByFacilityFacilityIdAndUsername(
            testFacility.getFacilityId(), "findme");

        assertTrue(found.isPresent());
        assertEquals("findme", found.get().getUsername());
        assertEquals(UserAccount.UserRole.SUPERVISOR, found.get().getRole());
    }

    @Test
    void findUserAccountByFacilityAndUsername_nonExistingUser_returnsEmpty() {
        Optional<UserAccount> found = userAccountRepository.findByFacilityFacilityIdAndUsername(
            testFacility.getFacilityId(), "nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void existsByFacilityAndUsername_existingUser_returnsTrue() {
        UserAccount userAccount = new UserAccount();
        userAccount.setFacility(testFacility);
        userAccount.setUsername("exists");
        userAccount.setPasswordHash("$2a$10$hash");
        userAccount.setRole(UserAccount.UserRole.STAFF);
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccountRepository.save(userAccount);

        boolean exists = userAccountRepository.existsByFacilityFacilityIdAndUsername(
            testFacility.getFacilityId(), "exists");

        assertTrue(exists);
    }

    @Test
    void existsByFacilityAndUsername_nonExistingUser_returnsFalse() {
        boolean exists = userAccountRepository.existsByFacilityFacilityIdAndUsername(
            testFacility.getFacilityId(), "doesnotexist");

        assertFalse(exists);
    }

    @Test
    void saveUserAccount_staffRoleWithStaffMemberId_success() {
        UserAccount userAccount = new UserAccount();
        userAccount.setFacility(testFacility);
        userAccount.setUsername("staffuser");
        userAccount.setPasswordHash("$2a$10$hash");
        userAccount.setRole(UserAccount.UserRole.STAFF);
        userAccount.setStaffMemberId(12345L);
        userAccount.setCreatedAt(LocalDateTime.now());

        UserAccount saved = userAccountRepository.save(userAccount);

        assertNotNull(saved.getUserAccountId());
        assertEquals(12345L, saved.getStaffMemberId());
    }

    @Test
    void saveUserAccount_managerRoleWithoutStaffMemberId_success() {
        UserAccount userAccount = new UserAccount();
        userAccount.setFacility(testFacility);
        userAccount.setUsername("manager");
        userAccount.setPasswordHash("$2a$10$hash");
        userAccount.setRole(UserAccount.UserRole.MANAGER);
        userAccount.setCreatedAt(LocalDateTime.now());

        UserAccount saved = userAccountRepository.save(userAccount);

        assertNotNull(saved.getUserAccountId());
        assertNull(saved.getStaffMemberId());
    }
}