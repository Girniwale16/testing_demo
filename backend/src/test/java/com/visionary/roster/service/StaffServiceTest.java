package com.visionary.roster.service;

import com.visionary.roster.audit.AuditEmitter;
import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.dto.UpdateStaffRequest;
import com.visionary.roster.exception.ForbiddenAccessException;
import com.visionary.roster.exception.ResourceNotFoundException;
import com.visionary.roster.model.Facility;
import com.visionary.roster.model.Staff;
import com.visionary.roster.repository.FacilityRepository;
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
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FacilityScopingService facilityScopingService;

    @Mock
    private RoleAuthorizationService roleAuthorizationService;

    @Mock
    private AuditEmitter auditEmitter;

    @InjectMocks
    private StaffService staffService;

    private Staff testStaff;
    private Facility testFacility;
    private CreateStaffRequest createRequest;
    private UpdateStaffRequest updateRequest;

    @BeforeEach
    void setUp() {
        MDC.put("correlationId", "test-correlation-id");

        testFacility = new Facility();
        testFacility.setFacilityId(1L);
        testFacility.setName("Test Facility");

        testStaff = new Staff();
        testStaff.setId(1L);
        testStaff.setFirstName("John");
        testStaff.setLastName("Doe");
        testStaff.setEmail("john.doe@example.com");
        testStaff.setRole("NURSE");
        testStaff.setFacility(testFacility);
        testStaff.setEmploymentStatus("ACTIVE");
        testStaff.setActive(true);
        testStaff.setDeactivated(false);

        createRequest = new CreateStaffRequest();
        createRequest.setFirstName("Jane");
        createRequest.setLastName("Smith");
        createRequest.setEmail("jane.smith@example.com");
        createRequest.setRole("DOCTOR");
        createRequest.setFacilityId(1L);
        createRequest.setEmploymentStatus("ACTIVE");

        updateRequest = new UpdateStaffRequest();
        updateRequest.setFirstName("John Updated");
        updateRequest.setLastName("Doe Updated");
        updateRequest.setEmail("john.updated@example.com");
        updateRequest.setRole("SENIOR_NURSE");
        updateRequest.setFacilityId(2L);
        updateRequest.setEmploymentStatus("PART_TIME");
    }

    @Test
    void listStaff_WhenIncludeDeactivatedTrue_ShouldReturnAllStaff() {
        // Arrange
        List<Staff> staffList = Arrays.asList(testStaff);
        Page<Staff> staffPage = new PageImpl<>(staffList);
        Pageable pageable = PageRequest.of(0, 10);

        when(staffRepository.findAll(any(Pageable.class))).thenReturn(staffPage);

        // Act
        Page<Staff> result = staffService.listStaff(0, 10, true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(staffRepository).findAll(any(Pageable.class));
        verify(staffRepository, never()).findByIsDeactivated(anyBoolean(), any(Pageable.class));
    }

    @Test
    void listStaff_WhenIncludeDeactivatedFalse_ShouldFilterByIsDeactivated() {
        // Arrange
        List<Staff> activeStaffList = Arrays.asList(testStaff);
        Page<Staff> staffPage = new PageImpl<>(activeStaffList);
        Pageable pageable = PageRequest.of(0, 10);

        when(staffRepository.findByIsDeactivated(eq(false), any(Pageable.class))).thenReturn(staffPage);

        // Act
        Page<Staff> result = staffService.listStaff(0, 10, false);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(staffRepository).findByIsDeactivated(eq(false), any(Pageable.class));
        verify(staffRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void listStaff_ShouldRetrieveCorrelationIdFromMDC() {
        // Arrange
        Page<Staff> staffPage = new PageImpl<>(Collections.emptyList());
        when(staffRepository.findAll(any(Pageable.class))).thenReturn(staffPage);

        // Act
        staffService.listStaff(0, 10, true);

        // Assert
        assertEquals("test-correlation-id", MDC.get("correlationId"));
    }

    @Test
    void createStaff_WhenValidRequest_ShouldCreateStaffAndEmitAuditEvent() {
        // Arrange
        when(staffRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(facilityRepository.findById(createRequest.getFacilityId())).thenReturn(Optional.of(testFacility));
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> {
            Staff staff = invocation.getArgument(0);
            staff.setId(2L);
            return staff;
        });

        // Act
        Staff result = staffService.createStaff(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(createRequest.getFirstName(), result.getFirstName());
        assertEquals(createRequest.getLastName(), result.getLastName());
        assertEquals(createRequest.getEmail(), result.getEmail());
        assertEquals(createRequest.getRole(), result.getRole());
        assertEquals(testFacility, result.getFacility());
        assertEquals(createRequest.getEmploymentStatus(), result.getEmploymentStatus());
        assertTrue(result.isActive());
        assertFalse(result.isDeactivated());

        verify(staffRepository).existsByEmail(createRequest.getEmail());
        verify(facilityRepository).findById(createRequest.getFacilityId());
        verify(staffRepository).save(any(Staff.class));
        verify(auditEmitter).emitStaffCreateEvent(eq(2L), eq("test-correlation-id"));
    }

    @Test
    void createStaff_WhenEmailAlreadyExists_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(staffRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> staffService.createStaff(createRequest));

        assertEquals("Email already exists for another staff member", exception.getMessage());
        verify(staffRepository).existsByEmail(createRequest.getEmail());
        verify(facilityRepository, never()).findById(anyLong());
        verify(staffRepository, never()).save(any(Staff.class));
        verify(auditEmitter, never()).emitStaffCreateEvent(anyLong(), anyString());
    }

    @Test
    void createStaff_WhenFacilityNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(staffRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(facilityRepository.findById(createRequest.getFacilityId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> staffService.createStaff(createRequest));

        assertTrue(exception.getMessage().contains("Facility"));
        verify(staffRepository).existsByEmail(createRequest.getEmail());
        verify(facilityRepository).findById(createRequest.getFacilityId());
        verify(staffRepository, never()).save(any(Staff.class));
        verify(auditEmitter, never()).emitStaffCreateEvent(anyLong(), anyString());
    }

    @Test
    void createStaff_ShouldRetrieveCorrelationIdFromMDC() {
        // Arrange
        when(staffRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(facilityRepository.findById(createRequest.getFacilityId())).thenReturn(Optional.of(testFacility));
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> {
            Staff staff = invocation.getArgument(0);
            staff.setId(2L);
            return staff;
        });

        // Act
        staffService.createStaff(createRequest);

        // Assert
        verify(auditEmitter).emitStaffCreateEvent(anyLong(), eq("test-correlation-id"));
    }

    @Test
    void updateStaff_WhenValidRequest_ShouldUpdateStaffAndEmitAuditEvent() {
        // Arrange
        Facility newFacility = new Facility();
        newFacility.setFacilityId(2L);
        newFacility.setName("New Facility");

        when(staffRepository.findById(1L)).thenReturn(Optional.of(testStaff));
        when(staffRepository.existsByEmailAndIdNot(updateRequest.getEmail(), 1L)).thenReturn(false);
        when(facilityRepository.findById(2L)).thenReturn(Optional.of(newFacility));
        when(staffRepository.save(any(Staff.class))).thenReturn(testStaff);

        // Act
        Staff result = staffService.updateStaff(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(updateRequest.getFirstName(), testStaff.getFirstName());
        assertEquals(updateRequest.getLastName(), testStaff.getLastName());
        assertEquals(updateRequest.getEmail(), testStaff.getEmail());
        assertEquals(updateRequest.getRole(), testStaff.getRole());
        assertEquals(newFacility, testStaff.getFacility());
        assertEquals(updateRequest.getEmploymentStatus(), testStaff.getEmploymentStatus());

        ArgumentCaptor<Map<String, Object>> changeMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditEmitter).emitStaffUpdateEvent(eq(1L), eq("test-correlation-id"), changeMapCaptor.capture());

        Map<String, Object> changeMap = changeMapCaptor.getValue();
        assertTrue(changeMap.containsKey("firstName"));
        assertTrue(changeMap.containsKey("lastName"));
        assertTrue(changeMap.containsKey("email"));
        assertTrue(changeMap.containsKey("role"));
        assertTrue(changeMap.containsKey("facilityId"));
        assertTrue(changeMap.containsKey("employmentStatus"));
    }

    @Test
    void updateStaff_WhenStaffNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> staffService.updateStaff(1L, updateRequest));

        assertTrue(exception.getMessage().contains("Staff"));
        verify(staffRepository).findById(1L);
        verify(staffRepository, never()).save(any(Staff.class));
        verify(auditEmitter, never()).emitStaffUpdateEvent(anyLong(), anyString(), any());
    }

    @Test
    void updateStaff_WhenEmailChangedAndAlreadyExists_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.of(testStaff));
        when(staffRepository.existsByEmailAndIdNot(updateRequest.getEmail(), 1L)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> staffService.updateStaff(1L, updateRequest));

        assertEquals("Email already exists for another staff member", exception.getMessage());
        verify(staffRepository).findById(1L);
        verify(staffRepository).existsByEmailAndIdNot(updateRequest.getEmail(), 1L);
        verify(staffRepository, never()).save(any(Staff.class));
        verify(auditEmitter, never()).emitStaffUpdateEvent(anyLong(), anyString(), any());
    }

    @Test
    void updateStaff_WhenNewFacilityNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.of(testStaff));
        when(staffRepository.existsByEmailAndIdNot(updateRequest.getEmail(), 1L)).thenReturn(false);
        when(facilityRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> staffService.updateStaff(1L, updateRequest));

        assertTrue(exception.getMessage().contains("Facility"));
        verify(facilityRepository).findById(2L);
        verify(staffRepository, never()).save(any(Staff.class));
        verify(auditEmitter, never()).emitStaffUpdateEvent(anyLong(), anyString(), any());
    }

    @Test
    void updateStaff_WhenNoFieldsChanged_ShouldNotIncludeChangesInAuditEvent() {
        // Arrange
        UpdateStaffRequest noChangeRequest = new UpdateStaffRequest();
        noChangeRequest.setFirstName(testStaff.getFirstName());
        noChangeRequest.setLastName(testStaff.getLastName());
        noChangeRequest.setEmail(testStaff.getEmail());
        noChangeRequest.setRole(testStaff.getRole());
        noChangeRequest.setFacilityId(testStaff.getFacility().getFacilityId());
        noChangeRequest.setEmploymentStatus(testStaff.getEmploymentStatus());

        when(staffRepository.findById(1L)).thenReturn(Optional.of(testStaff));
        when(staffRepository.save(any(Staff.class))).thenReturn(testStaff);

        // Act
        staffService.updateStaff(1L, noChangeRequest);

        // Assert
        ArgumentCaptor<Map<String, Object>> changeMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditEmitter).emitStaffUpdateEvent(eq(1L), eq("test-correlation-id"), changeMapCaptor.capture());

        Map<String, Object> changeMap = changeMapCaptor.getValue();
        assertTrue(changeMap.isEmpty());
    }

    @Test
    void updateStaff_ShouldRetrieveCorrelationIdFromMDC() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.of(testStaff));
        when(staffRepository.save(any(Staff.class))).thenReturn(testStaff);

        // Act
        staffService.updateStaff(1L, new UpdateStaffRequest());

        // Assert
        verify(auditEmitter).emitStaffUpdateEvent(anyLong(), eq("test-correlation-id"), any());
    }

    @Test
    void deactivateStaff_WhenValidId_ShouldSetDeactivatedTrueAndEmitAuditEvent() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.of(testStaff));
        when(staffRepository.save(any(Staff.class))).thenReturn(testStaff);

        // Act
        staffService.deactivateStaff(1L);

        // Assert
        assertTrue(testStaff.isDeactivated());
        assertFalse(testStaff.isActive());
        assertNotNull(testStaff.getDeactivationDate());
        assertEquals(LocalDate.now(), testStaff.getDeactivationDate());

        verify(staffRepository).findById(1L);
        verify(staffRepository).save(testStaff);
        verify(auditEmitter).emitStaffDeactivateEvent(eq(1L), eq("test-correlation-id"), eq("Staff deactivation"));
    }

    @Test
    void deactivateStaff_WhenStaffNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> staffService.deactivateStaff(1L));

        assertTrue(exception.getMessage().contains("Staff"));
        verify(staffRepository).findById(1L);
        verify(staffRepository, never()).save(any(Staff.class));
        verify(auditEmitter, never()).emitStaffDeactivateEvent(anyLong(), anyString(), anyString());
    }

    @Test
    void deactivateStaff_ShouldRetrieveCorrelationIdFromMDC() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.of(testStaff));
        when(staffRepository.save(any(Staff.class))).thenReturn(testStaff);

        // Act
        staffService.deactivateStaff(1L);

        // Assert
        verify(auditEmitter).emitStaffDeactivateEvent(anyLong(), eq("test-correlation-id"), anyString());
    }

    @Test
    void getStaffById_WhenValidIdAndFacilityAccess_ShouldReturnStaffResponse() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.of(testStaff));
        doNothing().when(facilityScopingService).validateFacilityAccess(testFacility.getFacilityId());

        // Act
        StaffResponse result = staffService.getStaffById(1L, 100L);

        // Assert
        assertNotNull(result);
        verify(staffRepository).findById(1L);
        verify(facilityScopingService).validateFacilityAccess(testFacility.getFacilityId());
    }

    @Test
    void getStaffById_WhenStaffNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> staffService.getStaffById(1L, 100L));

        assertTrue(exception.getMessage().contains("Staff"));
        verify(staffRepository).findById(1L);
        verify(facilityScopingService, never()).validateFacilityAccess(anyLong());
    }

    @Test
    void getStaffById_WhenFacilityAccessDenied_ShouldThrowForbiddenAccessException() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.of(testStaff));
        doThrow(new ForbiddenAccessException("Access denied")).when(facilityScopingService)
                .validateFacilityAccess(testFacility.getFacilityId());

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class,
                () -> staffService.getStaffById(1L, 100L));

        assertEquals("Access denied", exception.getMessage());
        verify(staffRepository).findById(1L);
        verify(facilityScopingService).validateFacilityAccess(testFacility.getFacilityId());
    }

    @Test
    void getStaffById_ShouldRetrieveCorrelationIdFromMDC() {
        // Arrange
        when(staffRepository.findById(1L)).thenReturn(Optional.of(testStaff));
        doNothing().when(facilityScopingService).validateFacilityAccess(testFacility.getFacilityId());

        // Act
        staffService.getStaffById(1L, 100L);

        // Assert
        assertEquals("test-correlation-id", MDC.get("correlationId"));
    }

    @Test
    void listActiveStaff_WhenValidFacilityIdAndAccess_ShouldReturnActiveStaffList() {
        // Arrange
        List<Staff> activeStaffList = Arrays.asList(testStaff);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(1L, "ACTIVE")).thenReturn(activeStaffList);
        doNothing().when(facilityScopingService).validateFacilityAccess(1L);

        // Act
        List<StaffResponse> result = staffService.listActiveStaff(1L, 100L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(facilityScopingService).validateFacilityAccess(1L);
        verify(staffRepository).findByFacilityIdAndEmploymentStatus(1L, "ACTIVE");
    }

    @Test
    void listActiveStaff_WhenFacilityAccessDenied_ShouldThrowForbiddenAccessException() {
        // Arrange
        doThrow(new ForbiddenAccessException("Access denied")).when(facilityScopingService)
                .validateFacilityAccess(1L);

        // Act & Assert
        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class,
                () -> staffService.listActiveStaff(1L, 100L));

        assertEquals("Access denied", exception.getMessage());
        verify(facilityScopingService).validateFacilityAccess(1L);
        verify(staffRepository, never()).findByFacilityIdAndEmploymentStatus(anyLong(), anyString());
    }

    @Test
    void listActiveStaff_ShouldRetrieveCorrelationIdFromMDC() {
        // Arrange
        List<Staff> activeStaffList = Arrays.asList(testStaff);
        when(staffRepository.findByFacilityIdAndEmploymentStatus(1L, "ACTIVE")).thenReturn(activeStaffList);
        doNothing().when(facilityScopingService).validateFacilityAccess(1L);

        // Act
        staffService.listActiveStaff(1L, 100L);

        // Assert
        assertEquals("test-correlation-id", MDC.get("correlationId"));
    }

    @Test
    void listActiveStaff_WhenNoActiveStaff_ShouldReturnEmptyList() {
        // Arrange
        when(staffRepository.findByFacilityIdAndEmploymentStatus(1L, "ACTIVE")).thenReturn(Collections.emptyList());
        doNothing().when(facilityScopingService).validateFacilityAccess(1L);

        // Act
        List<StaffResponse> result = staffService.listActiveStaff(1L, 100L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(facilityScopingService).validateFacilityAccess(1L);
        verify(staffRepository).findByFacilityIdAndEmploymentStatus(1L, "ACTIVE");
    }
}