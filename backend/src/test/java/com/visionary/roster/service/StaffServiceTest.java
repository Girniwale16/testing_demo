package com.visionary.roster.service;

import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.entity.StaffMember;
import com.visionary.roster.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StaffService.
 * 
 * Tests cover:
 * - Manager-only RBAC enforcement
 * - Facility scoping
 * - Date validation logic
 * - Employment status defaulting
 * - Staff creation and listing workflows
 */
@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private FacilityScopingService facilityScopingService;

    @Mock
    private RoleAuthorizationService roleAuthorizationService;

    @InjectMocks
    private StaffService staffService;

    private CreateStaffRequest validRequest;
    private StaffMember staffMember;
    private StaffResponse staffResponse;
    private Long facilityId;

    @BeforeEach
    void setUp() {
        facilityId = 100L;
        
        validRequest = new CreateStaffRequest();
        validRequest.setStartDate(LocalDate.of(2024, 1, 1));
        validRequest.setEndDate(LocalDate.of(2024, 12, 31));
        
        staffMember = new StaffMember();
        staffMember.setId(1L);
        staffMember.setFacilityId(facilityId);
        staffMember.setStartDate(LocalDate.of(2024, 1, 1));
        staffMember.setEndDate(LocalDate.of(2024, 12, 31));
        
        staffResponse = new StaffResponse();
        staffResponse.setId(1L);
        staffResponse.setFacilityId(facilityId);
    }

    // ==================== createStaff() Tests ====================

    @Test
    void createStaff_shouldEnforceManagerRole() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        // Act
        staffService.createStaff(validRequest);
        
        // Assert
        verify(roleAuthorizationService, times(1)).requireManagerRole();
    }

    @Test
    void createStaff_shouldCallRequireManagerRoleFirst() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        // Act
        staffService.createStaff(validRequest);
        
        // Assert - verify order of invocations
        var inOrder = inOrder(roleAuthorizationService, facilityScopingService, staffRepository);
        inOrder.verify(roleAuthorizationService).requireManagerRole();
        inOrder.verify(facilityScopingService).getCurrentUserFacilityId();
        inOrder.verify(staffRepository).save(any(StaffMember.class));
    }

    @Test
    void createStaff_shouldRetrieveFacilityId() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        // Act
        staffService.createStaff(validRequest);
        
        // Assert
        verify(facilityScopingService, times(1)).getCurrentUserFacilityId();
    }

    @Test
    void createStaff_shouldThrowExceptionWhenEndDateBeforeStartDate() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        
        CreateStaffRequest invalidRequest = new CreateStaffRequest();
        invalidRequest.setStartDate(LocalDate.of(2024, 12, 31));
        invalidRequest.setEndDate(LocalDate.of(2024, 1, 1));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            staffService.createStaff(invalidRequest);
        });
        
        assertEquals("End date must be on or after start date", exception.getMessage());
        verify(staffRepository, never()).save(any(StaffMember.class));
    }

    @Test
    void createStaff_shouldAcceptEndDateEqualToStartDate() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        CreateStaffRequest sameDataRequest = new CreateStaffRequest();
        sameDataRequest.setStartDate(LocalDate.of(2024, 6, 15));
        sameDataRequest.setEndDate(LocalDate.of(2024, 6, 15));
        
        // Act
        assertDoesNotThrow(() -> staffService.createStaff(sameDataRequest));
        
        // Assert
        verify(staffRepository, times(1)).save(any(StaffMember.class));
    }

    @Test
    void createStaff_shouldAcceptEndDateAfterStartDate() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        // Act
        assertDoesNotThrow(() -> staffService.createStaff(validRequest));
        
        // Assert
        verify(staffRepository, times(1)).save(any(StaffMember.class));
    }

    @Test
    void createStaff_shouldSkipValidationWhenStartDateIsNull() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        CreateStaffRequest requestWithNullStart = new CreateStaffRequest();
        requestWithNullStart.setStartDate(null);
        requestWithNullStart.setEndDate(LocalDate.of(2024, 12, 31));
        
        // Act
        assertDoesNotThrow(() -> staffService.createStaff(requestWithNullStart));
        
        // Assert
        verify(staffRepository, times(1)).save(any(StaffMember.class));
    }

    @Test
    void createStaff_shouldSkipValidationWhenEndDateIsNull() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        CreateStaffRequest requestWithNullEnd = new CreateStaffRequest();
        requestWithNullEnd.setStartDate(LocalDate.of(2024, 1, 1));
        requestWithNullEnd.setEndDate(null);
        
        // Act
        assertDoesNotThrow(() -> staffService.createStaff(requestWithNullEnd));
        
        // Assert
        verify(staffRepository, times(1)).save(any(StaffMember.class));
    }

    @Test
    void createStaff_shouldSkipValidationWhenBothDatesAreNull() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        CreateStaffRequest requestWithNullDates = new CreateStaffRequest();
        requestWithNullDates.setStartDate(null);
        requestWithNullDates.setEndDate(null);
        
        // Act
        assertDoesNotThrow(() -> staffService.createStaff(requestWithNullDates));
        
        // Assert
        verify(staffRepository, times(1)).save(any(StaffMember.class));
    }

    @Test
    void createStaff_shouldCallToEntityWithFacilityId() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        CreateStaffRequest spyRequest = spy(validRequest);
        
        // Act
        staffService.createStaff(spyRequest);
        
        // Assert
        verify(spyRequest, times(1)).toEntity(facilityId);
    }

    @Test
    void createStaff_shouldSaveStaffMemberEntity() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        // Act
        staffService.createStaff(validRequest);
        
        // Assert
        verify(staffRepository, times(1)).save(any(StaffMember.class));
    }

    @Test
    void createStaff_shouldReturnStaffResponse() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        // Act
        StaffResponse result = staffService.createStaff(validRequest);
        
        // Assert
        assertNotNull(result);
    }

    // ==================== listStaff() Tests ====================

    @Test
    void listStaff_shouldEnforceManagerRole() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        
        // Act
        staffService.listStaff("active");
        
        // Assert
        verify(roleAuthorizationService, times(1)).requireManagerRole();
    }

    @Test
    void listStaff_shouldCallRequireManagerRoleFirst() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        
        // Act
        staffService.listStaff("active");
        
        // Assert - verify order of invocations
        var inOrder = inOrder(roleAuthorizationService, facilityScopingService, staffRepository);
        inOrder.verify(roleAuthorizationService).requireManagerRole();
        inOrder.verify(facilityScopingService).getCurrentUserFacilityId();
        inOrder.verify(staffRepository).findByFacilityIdAndEmploymentStatus(anyLong(), anyString());
    }

    @Test
    void listStaff_shouldRetrieveFacilityId() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        
        // Act
        staffService.listStaff("active");
        
        // Assert
        verify(facilityScopingService, times(1)).getCurrentUserFacilityId();
    }

    @Test
    void listStaff_shouldDefaultToActiveWhenStatusIsNull() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, "active"))
                .thenReturn(Collections.emptyList());
        
        // Act
        staffService.listStaff(null);
        
        // Assert
        verify(staffRepository, times(1)).findByFacilityIdAndEmploymentStatus(facilityId, "active");
    }

    @Test
    void listStaff_shouldDefaultToActiveWhenStatusIsEmpty() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, "active"))
                .thenReturn(Collections.emptyList());
        
        // Act
        staffService.listStaff("");
        
        // Assert
        verify(staffRepository, times(1)).findByFacilityIdAndEmploymentStatus(facilityId, "active");
    }

    @Test
    void listStaff_shouldUseProvidedEmploymentStatus() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, "inactive"))
                .thenReturn(Collections.emptyList());
        
        // Act
        staffService.listStaff("inactive");
        
        // Assert
        verify(staffRepository, times(1)).findByFacilityIdAndEmploymentStatus(facilityId, "inactive");
    }

    @Test
    void listStaff_shouldQueryRepositoryWithFacilityIdAndStatus() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, "active"))
                .thenReturn(Collections.emptyList());
        
        // Act
        staffService.listStaff("active");
        
        // Assert
        verify(staffRepository, times(1)).findByFacilityIdAndEmploymentStatus(facilityId, "active");
    }

    @Test
    void listStaff_shouldReturnEmptyListWhenNoStaffFound() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, "active"))
                .thenReturn(Collections.emptyList());
        
        // Act
        List<StaffResponse> result = staffService.listStaff("active");
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listStaff_shouldTransformStaffMembersToStaffResponses() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        
        StaffMember member1 = new StaffMember();
        member1.setId(1L);
        member1.setFacilityId(facilityId);
        
        StaffMember member2 = new StaffMember();
        member2.setId(2L);
        member2.setFacilityId(facilityId);
        
        List<StaffMember> staffMembers = Arrays.asList(member1, member2);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, "active"))
                .thenReturn(staffMembers);
        
        // Act
        List<StaffResponse> result = staffService.listStaff("active");
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void listStaff_shouldReturnListOfStaffResponses() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        
        StaffMember member = new StaffMember();
        member.setId(1L);
        member.setFacilityId(facilityId);
        
        when(staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, "active"))
                .thenReturn(Arrays.asList(member));
        
        // Act
        List<StaffResponse> result = staffService.listStaff("active");
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ==================== Edge Case Tests ====================

    @Test
    void createStaff_shouldHandleDateValidationWithLeapYear() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        CreateStaffRequest leapYearRequest = new CreateStaffRequest();
        leapYearRequest.setStartDate(LocalDate.of(2024, 2, 29));
        leapYearRequest.setEndDate(LocalDate.of(2024, 3, 1));
        
        // Act
        assertDoesNotThrow(() -> staffService.createStaff(leapYearRequest));
        
        // Assert
        verify(staffRepository, times(1)).save(any(StaffMember.class));
    }

    @Test
    void createStaff_shouldHandleDateValidationWithYearBoundary() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.save(any(StaffMember.class))).thenReturn(staffMember);
        
        CreateStaffRequest yearBoundaryRequest = new CreateStaffRequest();
        yearBoundaryRequest.setStartDate(LocalDate.of(2023, 12, 31));
        yearBoundaryRequest.setEndDate(LocalDate.of(2024, 1, 1));
        
        // Act
        assertDoesNotThrow(() -> staffService.createStaff(yearBoundaryRequest));
        
        // Assert
        verify(staffRepository, times(1)).save(any(StaffMember.class));
    }

    @Test
    void listStaff_shouldHandleWhitespaceOnlyEmploymentStatus() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, "   "))
                .thenReturn(Collections.emptyList());
        
        // Act
        staffService.listStaff("   ");
        
        // Assert - whitespace is not considered empty, so it should be passed as-is
        verify(staffRepository, times(1)).findByFacilityIdAndEmploymentStatus(facilityId, "   ");
    }

    @Test
    void listStaff_shouldHandleDifferentEmploymentStatuses() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(facilityScopingService.getCurrentUserFacilityId()).thenReturn(facilityId);
        
        String[] statuses = {"active", "inactive", "terminated", "on-leave"};
        
        for (String status : statuses) {
            when(staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, status))
                    .thenReturn(Collections.emptyList());
            
            // Act
            staffService.listStaff(status);
            
            // Assert
            verify(staffRepository, times(1)).findByFacilityIdAndEmploymentStatus(facilityId, status);
        }
    }
}