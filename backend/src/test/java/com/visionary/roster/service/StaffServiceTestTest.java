package com.visionary.roster.service;

import com.visionary.roster.audit.AuditEmitter;
import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.StaffUpdateRequest;
import com.visionary.roster.dto.UpdateStaffRequest;
import com.visionary.roster.exception.FacilityAccessDeniedException;
import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.exception.ResourceNotFoundException;
import com.visionary.roster.model.Facility;
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
import org.springframework.dao.DataAccessException;

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

    private CreateStaffRequest createStaffRequest;
    private UpdateStaffRequest updateStaffRequest;
    private StaffUpdateRequest staffUpdateRequest;
    private Staff staff;
    private Facility facility;
    private StaffResponse staffResponse;

    @BeforeEach
    void setUp() {
        createStaffRequest = mock(CreateStaffRequest.class);
        updateStaffRequest = mock(UpdateStaffRequest.class);
        staffUpdateRequest = new StaffUpdateRequest();
        
        facility = new Facility();
        facility.setFacilityId(100L);
        
        staff = new Staff();
        staff.setId(1L);
        staff.setFacilityId(100L);
        staff.setEmploymentStatus("ACTIVE");
        staff.setStartDate(LocalDate.of(2023, 1, 1));
        staff.setEndDate(LocalDate.of(2024, 12, 31));
        staff.setFirstName("John");
        staff.setLastName("Doe");
        staff.setEmail("john.doe@example.com");
        staff.setRole("NURSE");
        staff.setFacility(facility);
        
        staffResponse = mock(StaffResponse.class);
    }

    // ==================== createStaff Tests ====================

    @Test
    void createStaff_Success_WithActiveStatus() {
        // Arrange
        Long facilityId = 100L;
        String userId = "user123";
        
        when(createStaffRequest.toEntity(facilityId)).thenReturn(staff);
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        when(StaffResponse.fromEntity(staff)).thenReturn(staffResponse);
        
        doNothing().when(roleAuthorizationService).checkPermission(userId, "STAFF_CREATE");
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);
        doNothing().when(auditEmitter).emitStaffCreateEvent(anyLong(), anyString(), anyLong());

        // Act
        StaffResponse result = staffService.createStaff(createStaffRequest, facilityId, userId);

        // Assert
        assertNotNull(result);
        verify(roleAuthorizationService).checkPermission(userId, "STAFF_CREATE");
        verify(facilityScopingService).validateFacilityAccess(userId, facilityId);
        verify(staffRepository).save(staff);
        verify(auditEmitter).emitStaffCreateEvent(staff.getId(), userId, facilityId);
    }

    @Test
    void createStaff_Success_WithPendingStatus() {
        // Arrange
        Long facilityId = 100L;
        String userId = "user123";
        staff.setEmploymentStatus("PENDING");
        
        when(createStaffRequest.toEntity(facilityId)).thenReturn(staff);
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        when(StaffResponse.fromEntity(staff)).thenReturn(staffResponse);
        
        doNothing().when(roleAuthorizationService).checkPermission(userId, "STAFF_CREATE");
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act
        StaffResponse result = staffService.createStaff(createStaffRequest, facilityId, userId);

        // Assert
        assertNotNull(result);
        verify(staffRepository).save(staff);
    }

    @Test
    void createStaff_ThrowsException_WhenInvalidEmploymentStatus() {
        // Arrange
        Long facilityId = 100L;
        String userId = "user123";
        staff.setEmploymentStatus("TERMINATED");
        
        when(createStaffRequest.toEntity(facilityId)).thenReturn(staff);
        doNothing().when(roleAuthorizationService).checkPermission(userId, "STAFF_CREATE");
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> staffService.createStaff(createStaffRequest, facilityId, userId));
        
        assertEquals("New staff must start with ACTIVE or PENDING status", exception.getMessage());
        verify(staffRepository, never()).save(any());
        verify(auditEmitter, never()).emitStaffCreateEvent(anyLong(), anyString(), anyLong());
    }

    @Test
    void createStaff_ThrowsException_WhenStartDateAfterEndDate() {
        // Arrange
        Long facilityId = 100L;
        String userId = "user123";
        staff.setStartDate(LocalDate.of(2024, 12, 31));
        staff.setEndDate(LocalDate.of(2023, 1, 1));
        
        when(createStaffRequest.toEntity(facilityId)).thenReturn(staff);
        doNothing().when(roleAuthorizationService).checkPermission(userId, "STAFF_CREATE");
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> staffService.createStaff(createStaffRequest, facilityId, userId));
        
        assertEquals("Start date must be before end date", exception.getMessage());
        verify(staffRepository, never()).save(any());
    }

    @Test
    void createStaff_ThrowsException_WhenStartDateEqualsEndDate() {
        // Arrange
        Long facilityId = 100L;
        String userId = "user123";
        LocalDate sameDate = LocalDate.of(2023, 6, 15);
        staff.setStartDate(sameDate);
        staff.setEndDate(sameDate);
        
        when(createStaffRequest.toEntity(facilityId)).thenReturn(staff);
        doNothing().when(roleAuthorizationService).checkPermission(userId, "STAFF_CREATE");
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> staffService.createStaff(createStaffRequest, facilityId, userId));
        
        assertEquals("Start date must be before end date", exception.getMessage());
    }

    @Test
    void createStaff_ThrowsException_WhenUserLacksPermission() {
        // Arrange
        Long facilityId = 100L;
        String userId = "user123";
        
        doThrow(new ForbiddenAccessException("User lacks STAFF_CREATE permission"))
            .when(roleAuthorizationService).checkPermission(userId, "STAFF_CREATE");

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.createStaff(createStaffRequest, facilityId, userId));
        
        verify(facilityScopingService, never()).validateFacilityAccess(anyString(), anyLong());
        verify(staffRepository, never()).save(any());
    }

    @Test
    void createStaff_ThrowsException_WhenUserLacksFacilityAccess() {
        // Arrange
        Long facilityId = 100L;
        String userId = "user123";
        
        doNothing().when(roleAuthorizationService).checkPermission(userId, "STAFF_CREATE");
        doThrow(new ForbiddenAccessException("User lacks facility access"))
            .when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.createStaff(createStaffRequest, facilityId, userId));
        
        verify(staffRepository, never()).save(any());
    }

    @Test
    void createStaff_Success_WithNullEndDate() {
        // Arrange
        Long facilityId = 100L;
        String userId = "user123";
        staff.setEndDate(null);
        
        when(createStaffRequest.toEntity(facilityId)).thenReturn(staff);
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        when(StaffResponse.fromEntity(staff)).thenReturn(staffResponse);
        
        doNothing().when(roleAuthorizationService).checkPermission(userId, "STAFF_CREATE");
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act
        StaffResponse result = staffService.createStaff(createStaffRequest, facilityId, userId);

        // Assert
        assertNotNull(result);
        verify(staffRepository).save(staff);
    }

    // ==================== updateStaff (String userId) Tests ====================

    @Test
    void updateStaff_Success_WithStringUserId() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        when(StaffResponse.fromEntity(staff)).thenReturn(staffResponse);
        
        doNothing().when(roleAuthorizationService).checkPermission(userId, "STAFF_UPDATE");
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());
        doNothing().when(staff).updateFromRequest(updateStaffRequest);
        doNothing().when(auditEmitter).emitStaffUpdateEvent(anyLong(), anyString(), anyMap());

        // Act
        StaffResponse result = staffService.updateStaff(staffId, updateStaffRequest, userId);

        // Assert
        assertNotNull(result);
        verify(roleAuthorizationService).checkPermission(userId, "STAFF_UPDATE");
        verify(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());
        verify(staffRepository).save(staff);
    }

    @Test
    void updateStaff_ThrowsException_WhenStaffNotFound() {
        // Arrange
        Long staffId = 999L;
        String userId = "user123";
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> staffService.updateStaff(staffId, updateStaffRequest, userId));
        
        assertTrue(exception.getMessage().contains("Staff"));
        verify(roleAuthorizationService, never()).checkPermission(anyString(), anyString());
    }

    @Test
    void updateStaff_ThrowsException_WhenUserLacksUpdatePermission() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        doThrow(new ForbiddenAccessException("User lacks STAFF_UPDATE permission"))
            .when(roleAuthorizationService).checkPermission(userId, "STAFF_UPDATE");

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.updateStaff(staffId, updateStaffRequest, userId));
        
        verify(staffRepository, never()).save(any());
    }

    @Test
    void updateStaff_ThrowsException_WhenTerminatedToActiveTransition() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        staff.setEmploymentStatus("TERMINATED");
        
        Staff updatedStaff = new Staff();
        updatedStaff.setId(staffId);
        updatedStaff.setFacilityId(100L);
        updatedStaff.setEmploymentStatus("ACTIVE");
        updatedStaff.setStartDate(LocalDate.of(2023, 1, 1));
        updatedStaff.setEndDate(LocalDate.of(2024, 12, 31));
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        doNothing().when(roleAuthorizationService).checkPermission(userId, "STAFF_UPDATE");
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());
        doAnswer(invocation -> {
            staff.setEmploymentStatus("ACTIVE");
            return null;
        }).when(staff).updateFromRequest(updateStaffRequest);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> staffService.updateStaff(staffId, updateStaffRequest, userId));
        
        assertEquals("Cannot change employment status from TERMINATED to ACTIVE without proper workflow", 
            exception.getMessage());
        verify(staffRepository, never()).save(any());
    }

    @Test
    void updateStaff_ThrowsException_WhenDateRangeInvalidAfterUpdate() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        doNothing().when(roleAuthorizationService).checkPermission(userId, "STAFF_UPDATE");
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());
        doAnswer(invocation -> {
            staff.setStartDate(LocalDate.of(2024, 12, 31));
            staff.setEndDate(LocalDate.of(2023, 1, 1));
            return null;
        }).when(staff).updateFromRequest(updateStaffRequest);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> staffService.updateStaff(staffId, updateStaffRequest, userId));
        
        assertEquals("Start date must be before end date", exception.getMessage());
    }

    @Test
    void updateStaff_Success_WithEmploymentStatusChange() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        staff.setEmploymentStatus("ACTIVE");
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        when(StaffResponse.fromEntity(staff)).thenReturn(staffResponse);
        
        doNothing().when(roleAuthorizationService).checkPermission(userId, "STAFF_UPDATE");
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());
        doAnswer(invocation -> {
            staff.setEmploymentStatus("PENDING");
            return null;
        }).when(staff).updateFromRequest(updateStaffRequest);

        // Act
        StaffResponse result = staffService.updateStaff(staffId, updateStaffRequest, userId);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Map> changedFieldsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditEmitter).emitStaffUpdateEvent(eq(staffId), eq(userId), changedFieldsCaptor.capture());
        
        Map<String, Object> changedFields = changedFieldsCaptor.getValue();
        assertTrue(changedFields.containsKey("employmentStatus"));
    }

    // ==================== getStaff Tests ====================

    @Test
    void getStaff_Success() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(StaffResponse.fromEntity(staff)).thenReturn(staffResponse);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());

        // Act
        StaffResponse result = staffService.getStaff(staffId, userId);

        // Assert
        assertNotNull(result);
        verify(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());
    }

    @Test
    void getStaff_ThrowsException_WhenStaffNotFound() {
        // Arrange
        Long staffId = 999L;
        String userId = "user123";
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> staffService.getStaff(staffId, userId));
    }

    @Test
    void getStaff_ThrowsException_WhenUserLacksFacilityAccess() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        doThrow(new ForbiddenAccessException("User lacks facility access"))
            .when(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.getStaff(staffId, userId));
    }

    // ==================== listStaff Tests ====================

    @Test
    void listStaff_Success() {
        // Arrange
        Long facilityId = 100L;
        String userId = "user123";
        List<Staff> staffList = Arrays.asList(staff, staff);
        
        when(staffRepository.findByFacilityId(facilityId)).thenReturn(staffList);
        when(StaffResponse.fromEntity(any(Staff.class))).thenReturn(staffResponse);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act
        List<StaffResponse> result = staffService.listStaff(facilityId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(facilityScopingService).validateFacilityAccess(userId, facilityId);
        verify(staffRepository).findByFacilityId(facilityId);
    }

    @Test
    void listStaff_ReturnsEmptyList_WhenNoStaffFound() {
        // Arrange
        Long facilityId = 100L;
        String userId = "user123";
        
        when(staffRepository.findByFacilityId(facilityId)).thenReturn(Arrays.asList());
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act
        List<StaffResponse> result = staffService.listStaff(facilityId, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listStaff_ThrowsException_WhenUserLacksFacilityAccess() {
        // Arrange
        Long facilityId = 100L;
        String userId = "user123";
        
        doThrow(new ForbiddenAccessException("User lacks facility access"))
            .when(facilityScopingService).validateFacilityAccess(userId, facilityId);

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.listStaff(facilityId, userId));
        
        verify(staffRepository, never()).findByFacilityId(anyLong());
    }

    // ==================== updateStaff (Long userId) Tests ====================

    @Test
    void updateStaffWithLongUserId_Success() {
        // Arrange
        Long staffId = 1L;
        Long requestingUserId = 123L;
        staffUpdateRequest.setFirstName("Jane");
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        when(StaffResponse.fromEntity(staff)).thenReturn(staffResponse);
        when(staffRepository.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(false);
        
        doNothing().when(roleAuthorizationService).requireManagerRole();
        doNothing().when(facilityScopingService).validateFacilityAccess(anyLong());
        doNothing().when(auditEmitter).emitStaffUpdateEvent(anyLong(), anyLong(), anyMap());

        // Act
        StaffResponse result = staffService.updateStaff(staffId, staffUpdateRequest, requestingUserId);

        // Assert
        assertNotNull(result);
        verify(roleAuthorizationService).requireManagerRole();
        verify(staffRepository).save(staff);
    }

    @Test
    void updateStaffWithLongUserId_ThrowsException_WhenNotManager() {
        // Arrange
        Long staffId = 1L;
        Long requestingUserId = 123L;
        
        doThrow(new ForbiddenAccessException("User is not a manager"))
            .when(roleAuthorizationService).requireManagerRole();

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.updateStaff(staffId, staffUpdateRequest, requestingUserId));
        
        verify(staffRepository, never()).findById(anyLong());
    }

    @Test
    void updateStaffWithLongUserId_ThrowsException_WhenStaffNotFound() {
        // Arrange
        Long staffId = 999L;
        Long requestingUserId = 123L;
        
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(staffRepository.findById(staffId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> staffService.updateStaff(staffId, staffUpdateRequest, requestingUserId));
    }

    @Test
    void updateStaffWithLongUserId_ThrowsException_WhenEmailAlreadyExists() {
        // Arrange
        Long staffId = 1L;
        Long requestingUserId = 123L;
        staffUpdateRequest.setEmail("existing@example.com");
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.existsByEmailAndIdNot("existing@example.com", staffId)).thenReturn(true);
        
        doNothing().when(roleAuthorizationService).requireManagerRole();
        doNothing().when(facilityScopingService).validateFacilityAccess(anyLong());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> staffService.updateStaff(staffId, staffUpdateRequest, requestingUserId));
        
        assertEquals("Email already exists for another staff member", exception.getMessage());
    }

    @Test
    void updateStaffWithLongUserId_Success_WithFacilityChange() {
        // Arrange
        Long staffId = 1L;
        Long requestingUserId = 123L;
        Long newFacilityId = 200L;
        staffUpdateRequest.setFacilityId(newFacilityId);
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        when(StaffResponse.fromEntity(staff)).thenReturn(staffResponse);
        when(staffRepository.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(false);
        
        doNothing().when(roleAuthorizationService).requireManagerRole();
        doNothing().when(facilityScopingService).validateFacilityAccess(anyLong());

        // Act
        StaffResponse result = staffService.updateStaff(staffId, staffUpdateRequest, requestingUserId);

        // Assert
        assertNotNull(result);
        verify(facilityScopingService, times(2)).validateFacilityAccess(anyLong());
    }

    @Test
    void updateStaffWithLongUserId_ThrowsException_WhenNewFacilityAccessDenied() {
        // Arrange
        Long staffId = 1L;
        Long requestingUserId = 123L;
        Long newFacilityId = 200L;
        staffUpdateRequest.setFacilityId(newFacilityId);
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        
        doNothing().when(roleAuthorizationService).requireManagerRole();
        doNothing().when(facilityScopingService).validateFacilityAccess(staff.getFacility().getFacilityId());
        doThrow(new ForbiddenAccessException("Access denied to new facility"))
            .when(facilityScopingService).validateFacilityAccess(newFacilityId);

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.updateStaff(staffId, staffUpdateRequest, requestingUserId));
    }

    // ==================== deactivateStaff Tests ====================

    @Test
    void testDeactivateStaff_Success() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        staff.setActive(true);
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());
        doNothing().when(auditEmitter).emitStaffUpdateEvent(eq(staffId), eq(userId), anyMap());

        // Act
        staffService.deactivateStaff(staffId, userId);

        // Assert
        verify(roleAuthorizationService).requireManagerRole(userId);
        verify(staffRepository).findById(staffId);
        verify(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());
        verify(staff).setActive(false);
        verify(staffRepository).save(staff);
        
        ArgumentCaptor<Map> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditEmitter).emitStaffUpdateEvent(eq(staffId), eq(userId), metadataCaptor.capture());
        Map<String, Object> metadata = metadataCaptor.getValue();
        assertEquals("DEACTIVATE", metadata.get("action"));
    }

    @Test
    void testDeactivateStaff_ThrowsForbiddenAccessException() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        
        doThrow(new ForbiddenAccessException("User lacks MANAGER role"))
            .when(roleAuthorizationService).requireManagerRole(userId);

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.deactivateStaff(staffId, userId));
        
        verify(staffRepository, never()).findById(anyLong());
        verify(staffRepository, never()).save(any());
    }

    @Test
    void testDeactivateStaff_ThrowsResourceNotFoundException() {
        // Arrange
        Long staffId = 999L;
        String userId = "user123";
        
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        when(staffRepository.findById(staffId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> staffService.deactivateStaff(staffId, userId));
        
        assertEquals("Staff member not found", exception.getMessage());
        verify(staffRepository, never()).save(any());
    }

    @Test
    void testDeactivateStaff_ThrowsFacilityAccessDeniedException() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doThrow(new FacilityAccessDeniedException("Facility access denied"))
            .when(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());

        // Act & Assert
        assertThrows(FacilityAccessDeniedException.class, 
            () -> staffService.deactivateStaff(staffId, userId));
        
        verify(staffRepository, never()).save(any());
    }

    @Test
    void testDeactivateStaff_Idempotency() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        staff.setActive(false);
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());
        doNothing().when(auditEmitter).emitStaffUpdateEvent(eq(staffId), eq(userId), anyMap());

        // Act
        staffService.deactivateStaff(staffId, userId);

        // Assert
        verify(staff, never()).setActive(false);
        verify(staffRepository).save(staff);
        verify(auditEmitter).emitStaffUpdateEvent(eq(staffId), eq(userId), anyMap());
    }

    @Test
    void testDeactivateStaff_TransactionalRollback() {
        // Arrange
        Long staffId = 1L;
        String userId = "user123";
        staff.setActive(true);
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenThrow(new DataAccessException("Database failure") {});
        
        doNothing().when(roleAuthorizationService).requireManagerRole(userId);
        doNothing().when(facilityScopingService).validateFacilityAccess(userId, staff.getFacilityId());

        // Act & Assert
        assertThrows(DataAccessException.class, 
            () -> staffService.deactivateStaff(staffId, userId));
        
        verify(staffRepository).save(staff);
        verify(auditEmitter, never()).emitStaffUpdateEvent(anyLong(), anyString(), anyMap());
    }

    @Test
    void deactivateStaff_Success_WithLongUserId() {
        // Arrange
        Long staffId = 1L;
        Long requestingUserId = 123L;
        staff.setActive(true);
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        
        doNothing().when(roleAuthorizationService).requireManagerRole();
        doNothing().when(facilityScopingService).validateFacilityAccess(anyLong());
        doNothing().when(auditEmitter).emitStaffDeactivateEvent(anyLong(), anyLong(), anyString());

        // Act
        staffService.deactivateStaff(staffId, requestingUserId);

        // Assert
        verify(roleAuthorizationService).requireManagerRole();
        verify(staffRepository).save(staff);
        verify(auditEmitter).emitStaffDeactivateEvent(staffId, requestingUserId, "Manager-initiated deactivation");
    }

    @Test
    void deactivateStaff_Idempotent_WhenAlreadyDeactivated() {
        // Arrange
        Long staffId = 1L;
        Long requestingUserId = 123L;
        staff.setActive(false);
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        
        doNothing().when(roleAuthorizationService).requireManagerRole();
        doNothing().when(facilityScopingService).validateFacilityAccess(anyLong());

        // Act
        staffService.deactivateStaff(staffId, requestingUserId);

        // Assert
        verify(staffRepository, never()).save(any());
        verify(auditEmitter, never()).emitStaffDeactivateEvent(anyLong(), anyLong(), anyString());
    }

    @Test
    void deactivateStaff_ThrowsException_WhenNotManager() {
        // Arrange
        Long staffId = 1L;
        Long requestingUserId = 123L;
        
        doThrow(new ForbiddenAccessException("User is not a manager"))
            .when(roleAuthorizationService).requireManagerRole();

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.deactivateStaff(staffId, requestingUserId));
        
        verify(staffRepository, never()).findById(anyLong());
    }

    @Test
    void deactivateStaff_ThrowsException_WhenStaffNotFound() {
        // Arrange
        Long staffId = 999L;
        Long requestingUserId = 123L;
        
        doNothing().when(roleAuthorizationService).requireManagerRole();
        when(staffRepository.findById(staffId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> staffService.deactivateStaff(staffId, requestingUserId));
    }

    @Test
    void deactivateStaff_ThrowsException_WhenFacilityAccessDenied() {
        // Arrange
        Long staffId = 1L;
        Long requestingUserId = 123L;
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        
        doNothing().when(roleAuthorizationService).requireManagerRole();
        doThrow(new ForbiddenAccessException("Facility access denied"))
            .when(facilityScopingService).validateFacilityAccess(anyLong());

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.deactivateStaff(staffId, requestingUserId));
        
        verify(staffRepository, never()).save(any());
    }

    // ==================== getStaffById Tests ====================

    @Test
    void getStaffById_Success() {
        // Arrange
        Long staffId = 1L;
        Long requestingUserId = 123L;
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(StaffResponse.fromEntity(staff)).thenReturn(staffResponse);
        doNothing().when(facilityScopingService).validateFacilityAccess(anyLong());

        // Act
        StaffResponse result = staffService.getStaffById(staffId, requestingUserId);

        // Assert
        assertNotNull(result);
        verify(facilityScopingService).validateFacilityAccess(staff.getFacility().getFacilityId());
    }

    @Test
    void getStaffById_ThrowsException_WhenStaffNotFound() {
        // Arrange
        Long staffId = 999L;
        Long requestingUserId = 123L;
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> staffService.getStaffById(staffId, requestingUserId));
    }

    @Test
    void getStaffById_ThrowsException_WhenFacilityAccessDenied() {
        // Arrange
        Long staffId = 1L;
        Long requestingUserId = 123L;
        
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        doThrow(new ForbiddenAccessException("Facility access denied"))
            .when(facilityScopingService).validateFacilityAccess(anyLong());

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.getStaffById(staffId, requestingUserId));
    }

    // ==================== listActiveStaff Tests ====================

    @Test
    void listActiveStaff_Success() {
        // Arrange
        Long facilityId = 100L;
        Long requestingUserId = 123L;
        List<Staff> activeStaffList = Arrays.asList(staff, staff);
        
        when(staffRepository.findByFacility_FacilityIdAndEmploymentStatus(facilityId, "ACTIVE"))
            .thenReturn(activeStaffList);
        when(StaffResponse.fromEntity(any(Staff.class))).thenReturn(staffResponse);
        doNothing().when(facilityScopingService).validateFacilityAccess(facilityId);

        // Act
        List<StaffResponse> result = staffService.listActiveStaff(facilityId, requestingUserId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(facilityScopingService).validateFacilityAccess(facilityId);
        verify(staffRepository).findByFacility_FacilityIdAndEmploymentStatus(facilityId, "ACTIVE");
    }

    @Test
    void listActiveStaff_ReturnsEmptyList_WhenNoActiveStaff() {
        // Arrange
        Long facilityId = 100L;
        Long requestingUserId = 123L;
        
        when(staffRepository.findByFacility_FacilityIdAndEmploymentStatus(facilityId, "ACTIVE"))
            .thenReturn(Arrays.asList());
        doNothing().when(facilityScopingService).validateFacilityAccess(facilityId);

        // Act
        List<StaffResponse> result = staffService.listActiveStaff(facilityId, requestingUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listActiveStaff_ThrowsException_WhenFacilityAccessDenied() {
        // Arrange
        Long facilityId = 100L;
        Long requestingUserId = 123L;
        
        doThrow(new ForbiddenAccessException("Facility access denied"))
            .when(facilityScopingService).validateFacilityAccess(facilityId);

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, 
            () -> staffService.listActiveStaff(facilityId, requestingUserId));
        
        verify(staffRepository, never()).findByFacility_FacilityIdAndEmploymentStatus(anyLong(), anyString());
    }
}