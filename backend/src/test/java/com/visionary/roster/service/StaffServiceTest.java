package com.visionary.roster.service;

import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.StaffUpdateRequest;
import com.visionary.roster.entity.Facility;
import com.visionary.roster.entity.Staff;
import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.exception.ResourceNotFoundException;
import com.visionary.roster.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private FacilityScopingService facilityScopingService;

    @Mock
    private RoleAuthorizationService roleAuthorizationService;

    @Mock
    private AuditEmitter auditEmitter;

    @InjectMocks
    private StaffService staffService;

    private Staff mockStaff;
    private Facility mockFacility;
    private StaffUpdateRequest updateRequest;
    private Long staffId = 1L;
    private Long requestingUserId = 100L;
    private Long facilityId = 10L;

    @BeforeEach
    void setUp() {
        mockFacility = new Facility();
        mockFacility.setId(facilityId);

        mockStaff = new Staff();
        mockStaff.setId(staffId);
        mockStaff.setFirstName("John");
        mockStaff.setLastName("Doe");
        mockStaff.setEmail("john.doe@example.com");
        mockStaff.setRole("NURSE");
        mockStaff.setEmploymentStatus("ACTIVE");
        mockStaff.setFacility(mockFacility);
        mockStaff.setActive(true);

        updateRequest = new StaffUpdateRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@example.com");
        updateRequest.setRole("DOCTOR");
        updateRequest.setEmploymentStatus("ACTIVE");
    }

    // ==================== updateStaff Tests ====================

    @Test
    void updateStaff_Success_AllFieldsUpdated() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        when(staffRepository.existsByEmailAndIdNot(updateRequest.getEmail(), staffId)).thenReturn(false);
        when(staffRepository.save(any(Staff.class))).thenReturn(mockStaff);
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        doNothing().when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
        doNothing().when(auditEmitter).emitStaffUpdateEvent(anyLong(), anyLong(), anyMap());

        // Act
        StaffResponse response = staffService.updateStaff(staffId, updateRequest, requestingUserId);

        // Assert
        assertNotNull(response);
        verify(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        verify(staffRepository).findById(staffId);
        verify(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
        verify(staffRepository).existsByEmailAndIdNot(updateRequest.getEmail(), staffId);
        verify(staffRepository).save(mockStaff);
        verify(auditEmitter).emitStaffUpdateEvent(eq(staffId), eq(requestingUserId), anyMap());
    }

    @Test
    void updateStaff_ThrowsForbiddenAccessException_WhenUserNotManager() {
        // Arrange
        doThrow(new ForbiddenAccessException("User does not have MANAGER role"))
                .when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () ->
                staffService.updateStaff(staffId, updateRequest, requestingUserId)
        );

        verify(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        verify(staffRepository, never()).findById(anyLong());
        verify(staffRepository, never()).save(any());
    }

    @Test
    void updateStaff_ThrowsResourceNotFoundException_WhenStaffNotFound() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.empty());
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                staffService.updateStaff(staffId, updateRequest, requestingUserId)
        );

        verify(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        verify(staffRepository).findById(staffId);
        verify(staffRepository, never()).save(any());
    }

    @Test
    void updateStaff_ThrowsForbiddenAccessException_WhenFacilityAccessDenied() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        doThrow(new ForbiddenAccessException("Access denied to facility"))
                .when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () ->
                staffService.updateStaff(staffId, updateRequest, requestingUserId)
        );

        verify(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
        verify(staffRepository, never()).save(any());
    }

    @Test
    void updateStaff_ValidatesNewFacilityAccess_WhenFacilityIdChanged() {
        // Arrange
        Long newFacilityId = 20L;
        updateRequest.setFacilityId(newFacilityId);

        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        when(staffRepository.existsByEmailAndIdNot(updateRequest.getEmail(), staffId)).thenReturn(false);
        when(staffRepository.save(any(Staff.class))).thenReturn(mockStaff);
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        doNothing().when(facilityScopingService).validateFacilityAccess(eq(requestingUserId), anyLong());
        doNothing().when(auditEmitter).emitStaffUpdateEvent(anyLong(), anyLong(), anyMap());

        // Act
        staffService.updateStaff(staffId, updateRequest, requestingUserId);

        // Assert
        verify(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
        verify(facilityScopingService).validateFacilityAccess(requestingUserId, newFacilityId);
    }

    @Test
    void updateStaff_ThrowsForbiddenAccessException_WhenNewFacilityAccessDenied() {
        // Arrange
        Long newFacilityId = 20L;
        updateRequest.setFacilityId(newFacilityId);

        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        doNothing().when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
        doThrow(new ForbiddenAccessException("Access denied to new facility"))
                .when(facilityScopingService).validateFacilityAccess(requestingUserId, newFacilityId);

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () ->
                staffService.updateStaff(staffId, updateRequest, requestingUserId)
        );

        verify(facilityScopingService).validateFacilityAccess(requestingUserId, newFacilityId);
        verify(staffRepository, never()).save(any());
    }

    @Test
    void updateStaff_ThrowsIllegalArgumentException_WhenEmailAlreadyExists() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        when(staffRepository.existsByEmailAndIdNot(updateRequest.getEmail(), staffId)).thenReturn(true);
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        doNothing().when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                staffService.updateStaff(staffId, updateRequest, requestingUserId)
        );

        assertEquals("Email already exists for another staff member", exception.getMessage());
        verify(staffRepository).existsByEmailAndIdNot(updateRequest.getEmail(), staffId);
        verify(staffRepository, never()).save(any());
    }

    @Test
    void updateStaff_TracksChangesInAuditMap() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        when(staffRepository.existsByEmailAndIdNot(updateRequest.getEmail(), staffId)).thenReturn(false);
        when(staffRepository.save(any(Staff.class))).thenReturn(mockStaff);
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        doNothing().when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);

        ArgumentCaptor<Map<String, Object>> changeMapCaptor = ArgumentCaptor.forClass(Map.class);

        // Act
        staffService.updateStaff(staffId, updateRequest, requestingUserId);

        // Assert
        verify(auditEmitter).emitStaffUpdateEvent(eq(staffId), eq(requestingUserId), changeMapCaptor.capture());
        Map<String, Object> changeMap = changeMapCaptor.getValue();

        assertTrue(changeMap.containsKey("firstName"));
        assertTrue(changeMap.containsKey("lastName"));
        assertTrue(changeMap.containsKey("email"));
        assertTrue(changeMap.containsKey("role"));
    }

    @Test
    void updateStaff_DoesNotTrackUnchangedFields() {
        // Arrange
        updateRequest.setFirstName("John"); // Same as current
        updateRequest.setLastName("Doe"); // Same as current

        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        when(staffRepository.existsByEmailAndIdNot(updateRequest.getEmail(), staffId)).thenReturn(false);
        when(staffRepository.save(any(Staff.class))).thenReturn(mockStaff);
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        doNothing().when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);

        ArgumentCaptor<Map<String, Object>> changeMapCaptor = ArgumentCaptor.forClass(Map.class);

        // Act
        staffService.updateStaff(staffId, updateRequest, requestingUserId);

        // Assert
        verify(auditEmitter).emitStaffUpdateEvent(eq(staffId), eq(requestingUserId), changeMapCaptor.capture());
        Map<String, Object> changeMap = changeMapCaptor.getValue();

        assertFalse(changeMap.containsKey("firstName"));
        assertFalse(changeMap.containsKey("lastName"));
    }

    // ==================== deactivateStaff Tests ====================

    @Test
    void deactivateStaff_Success() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        when(staffRepository.save(any(Staff.class))).thenReturn(mockStaff);
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        doNothing().when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
        doNothing().when(auditEmitter).emitStaffDeactivateEvent(anyLong(), anyLong(), anyString());

        // Act
        staffService.deactivateStaff(staffId, requestingUserId);

        // Assert
        verify(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        verify(staffRepository).findById(staffId);
        verify(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
        verify(staffRepository).save(mockStaff);
        verify(auditEmitter).emitStaffDeactivateEvent(staffId, requestingUserId, "Manager-initiated deactivation");
    }

    @Test
    void deactivateStaff_ThrowsForbiddenAccessException_WhenUserNotManager() {
        // Arrange
        doThrow(new ForbiddenAccessException("User does not have MANAGER role"))
                .when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () ->
                staffService.deactivateStaff(staffId, requestingUserId)
        );

        verify(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        verify(staffRepository, never()).findById(anyLong());
        verify(staffRepository, never()).save(any());
    }

    @Test
    void deactivateStaff_ThrowsResourceNotFoundException_WhenStaffNotFound() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.empty());
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                staffService.deactivateStaff(staffId, requestingUserId)
        );

        verify(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        verify(staffRepository).findById(staffId);
        verify(staffRepository, never()).save(any());
    }

    @Test
    void deactivateStaff_ThrowsForbiddenAccessException_WhenFacilityAccessDenied() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        doThrow(new ForbiddenAccessException("Access denied to facility"))
                .when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () ->
                staffService.deactivateStaff(staffId, requestingUserId)
        );

        verify(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
        verify(staffRepository, never()).save(any());
    }

    @Test
    void deactivateStaff_Idempotent_WhenStaffAlreadyDeactivated() {
        // Arrange
        mockStaff.setActive(false);
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        doNothing().when(roleAuthorizationService).requireRole(requestingUserId, "MANAGER");
        doNothing().when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);

        // Act
        staffService.deactivateStaff(staffId, requestingUserId);

        // Assert
        verify(staffRepository).findById(staffId);
        verify(staffRepository, never()).save(any());
        verify(auditEmitter, never()).emitStaffDeactivateEvent(anyLong(), anyLong(), anyString());
    }

    // ==================== getStaffById Tests ====================

    @Test
    void getStaffById_Success() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        doNothing().when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);

        // Act
        StaffResponse response = staffService.getStaffById(staffId, requestingUserId);

        // Assert
        assertNotNull(response);
        verify(staffRepository).findById(staffId);
        verify(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
    }

    @Test
    void getStaffById_ThrowsResourceNotFoundException_WhenStaffNotFound() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                staffService.getStaffById(staffId, requestingUserId)
        );

        verify(staffRepository).findById(staffId);
        verify(facilityScopingService, never()).validateFacilityAccess(anyLong(), anyLong());
    }

    @Test
    void getStaffById_ThrowsForbiddenAccessException_WhenFacilityAccessDenied() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        doThrow(new ForbiddenAccessException("Access denied to facility"))
                .when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () ->
                staffService.getStaffById(staffId, requestingUserId)
        );

        verify(staffRepository).findById(staffId);
        verify(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
    }

    // ==================== listActiveStaff Tests ====================

    @Test
    void listActiveStaff_Success() {
        // Arrange
        Staff staff1 = new Staff();
        staff1.setId(1L);
        staff1.setEmploymentStatus("ACTIVE");
        staff1.setFacility(mockFacility);

        Staff staff2 = new Staff();
        staff2.setId(2L);
        staff2.setEmploymentStatus("ACTIVE");
        staff2.setFacility(mockFacility);

        List<Staff> activeStaffList = Arrays.asList(staff1, staff2);

        when(staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, "ACTIVE")).thenReturn(activeStaffList);
        doNothing().when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);

        // Act
        List<StaffResponse> responses = staffService.listActiveStaff(facilityId, requestingUserId);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
        verify(staffRepository).findByFacilityIdAndEmploymentStatus(facilityId, "ACTIVE");
    }

    @Test
    void listActiveStaff_ThrowsForbiddenAccessException_WhenFacilityAccessDenied() {
        // Arrange
        doThrow(new ForbiddenAccessException("Access denied to facility"))
                .when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () ->
                staffService.listActiveStaff(facilityId, requestingUserId)
        );

        verify(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
        verify(staffRepository, never()).findByFacilityIdAndEmploymentStatus(anyLong(), anyString());
    }

    @Test
    void listActiveStaff_ReturnsEmptyList_WhenNoActiveStaff() {
        // Arrange
        when(staffRepository.findByFacilityIdAndEmploymentStatus(facilityId, "ACTIVE")).thenReturn(Arrays.asList());
        doNothing().when(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);

        // Act
        List<StaffResponse> responses = staffService.listActiveStaff(facilityId, requestingUserId);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(facilityScopingService).validateFacilityAccess(requestingUserId, facilityId);
        verify(staffRepository).findByFacilityIdAndEmploymentStatus(facilityId, "ACTIVE");
    }
}