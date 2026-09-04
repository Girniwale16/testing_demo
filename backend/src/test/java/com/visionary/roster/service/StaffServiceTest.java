package com.visionary.roster.service;

import com.visionary.roster.audit.AuditEmitter;
import com.visionary.roster.exception.FacilityAccessDeniedException;
import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.exception.ResourceNotFoundException;
import com.visionary.roster.model.Staff;
import com.visionary.roster.repository.StaffRepository;
import com.visionary.roster.security.FacilityScopingService;
import com.visionary.roster.security.RoleAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StaffService.deactivateStaff method.
 * Tests cover authorization, facility scoping, idempotency, audit trail, and exception handling.
 */
@ExtendWith(MockitoExtension.class)
class StaffServiceDeactivateStaffTest {

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

    private Staff activeStaff;
    private Staff inactiveStaff;
    private Long staffId;
    private Long userId;
    private Long facilityId;

    @BeforeEach
    void setUp() {
        staffId = 100L;
        userId = 200L;
        facilityId = 300L;

        activeStaff = new Staff();
        activeStaff.setId(staffId);
        activeStaff.setFacilityId(facilityId);
        activeStaff.setActive(true);
        activeStaff.setEmail("active@example.com");

        inactiveStaff = new Staff();
        inactiveStaff.setId(staffId);
        inactiveStaff.setFacilityId(facilityId);
        inactiveStaff.setActive(false);
        inactiveStaff.setEmail("inactive@example.com");
    }

    @Test
    void deactivateStaff_shouldCallRequireManagerRoleWithUserId() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(activeStaff));
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);
        when(staffRepository.save(any(Staff.class))).thenReturn(activeStaff);

        // Act
        staffService.deactivateStaff(staffId, userId);

        // Assert
        verify(roleAuthorizationService, times(1)).requireManagerRole(userId);
    }

    @Test
    void deactivateStaff_shouldThrowForbiddenAccessException_whenManagerRoleValidationFails() {
        // Arrange
        doThrow(new ForbiddenAccessException("User does not have MANAGER role"))
                .when(roleAuthorizationService).requireManagerRole(userId);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class, () -> {
            staffService.deactivateStaff(staffId, userId);
        });

        assertEquals("User does not have MANAGER role", exception.getMessage());
        verify(staffRepository, never()).findById(anyLong());
        verify(facilityScopingService, never()).validateFacilityAccess(anyLong(), anyLong());
        verify(staffRepository, never()).save(any(Staff.class));
        verify(auditEmitter, never()).emitStaffUpdateEvent(anyLong(), anyLong(), anyMap());
    }

    @Test
    void deactivateStaff_shouldThrowResourceNotFoundException_whenStaffNotFound() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        when(staffRepository.findById(staffId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            staffService.deactivateStaff(staffId, userId);
        });

        assertEquals("Staff member not found", exception.getMessage());
        verify(roleAuthorizationService, times(1)).requireManagerRole(userId);
        verify(staffRepository, times(1)).findById(staffId);
        verify(facilityScopingService, never()).validateFacilityAccess(anyLong(), anyLong());
        verify(staffRepository, never()).save(any(Staff.class));
        verify(auditEmitter, never()).emitStaffUpdateEvent(anyLong(), anyLong(), anyMap());
    }

    @Test
    void deactivateStaff_shouldCallValidateFacilityAccessWithUserIdAndFacilityId() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(activeStaff));
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);
        when(staffRepository.save(any(Staff.class))).thenReturn(activeStaff);

        // Act
        staffService.deactivateStaff(staffId, userId);

        // Assert
        verify(facilityScopingService, times(1)).validateFacilityAccess(userId, facilityId);
    }

    @Test
    void deactivateStaff_shouldThrowFacilityAccessDeniedException_whenFacilityAccessValidationFails() {
        // Arrange
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(activeStaff));
        doThrow(new FacilityAccessDeniedException("User does not have access to facility"))
                .when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act & Assert
        FacilityAccessDeniedException exception = assertThrows(FacilityAccessDeniedException.class, () -> {
            staffService.deactivateStaff(staffId, userId);
        });

        assertEquals("User does not have access to facility", exception.getMessage());
        verify(roleAuthorizationService, times(1)).requireManagerRole(userId);
        verify(staffRepository, times(1)).findById(staffId);
        verify(facilityScopingService, times(1)).validateFacilityAccess(userId, facilityId);
        verify(staffRepository, never()).save(any(Staff.class));
        verify(auditEmitter, never()).emitStaffUpdateEvent(anyLong(), anyLong(), anyMap());
    }

    @Test
    void deactivateStaff_shouldSetActiveToFalse_whenStaffIsActive() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(activeStaff));
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);
        when(staffRepository.save(any(Staff.class))).thenReturn(activeStaff);

        // Act
        staffService.deactivateStaff(staffId, userId);

        // Assert
        ArgumentCaptor<Staff> staffCaptor = ArgumentCaptor.forClass(Staff.class);
        verify(staffRepository, times(1)).save(staffCaptor.capture());
        Staff savedStaff = staffCaptor.getValue();
        assertFalse(savedStaff.isActive());
    }

    @Test
    void deactivateStaff_shouldSaveStaffEntity_whenStaffIsActive() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(activeStaff));
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);
        when(staffRepository.save(any(Staff.class))).thenReturn(activeStaff);

        // Act
        staffService.deactivateStaff(staffId, userId);

        // Assert
        verify(staffRepository, times(1)).save(activeStaff);
    }

    @Test
    void deactivateStaff_shouldEmitAuditEventWithCorrectMetadata_whenStaffIsActive() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(activeStaff));
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);
        when(staffRepository.save(any(Staff.class))).thenReturn(activeStaff);

        // Act
        staffService.deactivateStaff(staffId, userId);

        // Assert
        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditEmitter, times(1)).emitStaffUpdateEvent(eq(staffId), eq(userId), metadataCaptor.capture());

        Map<String, Object> metadata = metadataCaptor.getValue();
        assertEquals("MANAGER", metadata.get("userRole"));
        assertEquals("MANAGER", metadata.get("requiredRole"));
        assertEquals("AUTHORIZED", metadata.get("authorizationResult"));
        assertEquals("DEACTIVATE", metadata.get("action"));
        assertEquals(4, metadata.size());
    }

    @Test
    void deactivateStaff_shouldImplementIdempotency_whenStaffIsAlreadyInactive() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(inactiveStaff));
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act
        staffService.deactivateStaff(staffId, userId);

        // Assert
        verify(roleAuthorizationService, times(1)).requireManagerRole(userId);
        verify(staffRepository, times(1)).findById(staffId);
        verify(facilityScopingService, times(1)).validateFacilityAccess(userId, facilityId);
        verify(staffRepository, never()).save(any(Staff.class));
        verify(auditEmitter, never()).emitStaffUpdateEvent(anyLong(), anyLong(), anyMap());
    }

    @Test
    void deactivateStaff_shouldReturnSuccessfully_whenStaffIsAlreadyInactive() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(inactiveStaff));
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act & Assert - should not throw any exception
        assertDoesNotThrow(() -> {
            staffService.deactivateStaff(staffId, userId);
        });
    }

    @Test
    void deactivateStaff_shouldBeTransactional_rollbackOnException() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(activeStaff));
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);
        when(staffRepository.save(any(Staff.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            staffService.deactivateStaff(staffId, userId);
        });

        verify(staffRepository, times(1)).save(any(Staff.class));
        verify(auditEmitter, never()).emitStaffUpdateEvent(anyLong(), anyLong(), anyMap());
    }

    @Test
    void deactivateStaff_shouldExtractFacilityIdFromStaffEntity() {
        // Arrange
        Staff staffWithDifferentFacility = new Staff();
        staffWithDifferentFacility.setId(staffId);
        staffWithDifferentFacility.setFacilityId(999L);
        staffWithDifferentFacility.setActive(true);

        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staffWithDifferentFacility));
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, 999L);
        when(staffRepository.save(any(Staff.class))).thenReturn(staffWithDifferentFacility);

        // Act
        staffService.deactivateStaff(staffId, userId);

        // Assert
        verify(facilityScopingService, times(1)).validateFacilityAccess(userId, 999L);
    }

    @Test
    void deactivateStaff_shouldCompleteFullWorkflow_whenAllValidationsPassed() {
        // Arrange
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(activeStaff));
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);
        when(staffRepository.save(any(Staff.class))).thenReturn(activeStaff);

        // Act
        staffService.deactivateStaff(staffId, userId);

        // Assert - verify complete workflow execution order
        verify(roleAuthorizationService, times(1)).requireManagerRole(userId);
        verify(staffRepository, times(1)).findById(staffId);
        verify(facilityScopingService, times(1)).validateFacilityAccess(userId, facilityId);
        verify(staffRepository, times(1)).save(activeStaff);
        verify(auditEmitter, times(1)).emitStaffUpdateEvent(eq(staffId), eq(userId), anyMap());
    }

    @Test
    void deactivateStaff_shouldNotModifyOtherStaffProperties_whenDeactivating() {
        // Arrange
        activeStaff.setEmail("test@example.com");
        activeStaff.setEmploymentStatus("ACTIVE");
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(activeStaff));
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);
        when(staffRepository.save(any(Staff.class))).thenReturn(activeStaff);

        // Act
        staffService.deactivateStaff(staffId, userId);

        // Assert
        ArgumentCaptor<Staff> staffCaptor = ArgumentCaptor.forClass(Staff.class);
        verify(staffRepository, times(1)).save(staffCaptor.capture());
        Staff savedStaff = staffCaptor.getValue();
        
        assertFalse(savedStaff.isActive());
        assertEquals("test@example.com", savedStaff.getEmail());
        assertEquals("ACTIVE", savedStaff.getEmploymentStatus());
        assertEquals(facilityId, savedStaff.getFacilityId());
    }
}