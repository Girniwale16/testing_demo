package com.visionary.roster.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Staff entity class.
 * Ensures 100% test coverage of all business logic.
 */
class StaffTest {

    private Staff staff;
    private CreateStaffRequest createRequest;
    private UpdateStaffRequest updateRequest;

    @BeforeEach
    void setUp() {
        staff = new Staff();
        createRequest = new CreateStaffRequest();
        updateRequest = new UpdateStaffRequest();
    }

    @Test
    @DisplayName("Test no-args constructor creates empty Staff instance")
    void testNoArgsConstructor() {
        Staff emptyStaff = new Staff();
        assertNotNull(emptyStaff);
        assertNull(emptyStaff.getId());
        assertNull(emptyStaff.getFirstName());
        assertNull(emptyStaff.getLastName());
    }

    @Test
    @DisplayName("Test all-args constructor sets all fields correctly")
    void testAllArgsConstructor() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        Staff staffWithArgs = new Staff(
                1L, "John", "Doe", "555-1234", "Nurse",
                "Full-Time", startDate, endDate, 100L,
                createdAt, updatedAt, "admin", "admin"
        );

        assertEquals(1L, staffWithArgs.getId());
        assertEquals("John", staffWithArgs.getFirstName());
        assertEquals("Doe", staffWithArgs.getLastName());
        assertEquals("555-1234", staffWithArgs.getContact());
        assertEquals("Nurse", staffWithArgs.getRole());
        assertEquals("Full-Time", staffWithArgs.getEmploymentStatus());
        assertEquals(startDate, staffWithArgs.getStartDate());
        assertEquals(endDate, staffWithArgs.getEndDate());
        assertEquals(100L, staffWithArgs.getFacilityId());
        assertEquals(createdAt, staffWithArgs.getCreatedAt());
        assertEquals(updatedAt, staffWithArgs.getUpdatedAt());
        assertEquals("admin", staffWithArgs.getCreatedBy());
        assertEquals("admin", staffWithArgs.getUpdatedBy());
    }

    @Test
    @DisplayName("Test toEntity creates Staff from CreateStaffRequest with all fields")
    void testToEntityWithAllFields() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        createRequest.setFirstName("Jane");
        createRequest.setLastName("Smith");
        createRequest.setContact("555-5678");
        createRequest.setRole("Doctor");
        createRequest.setEmploymentStatus("Part-Time");
        createRequest.setStartDate(startDate);
        createRequest.setEndDate(endDate);

        Staff result = Staff.toEntity(createRequest, 200L);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("555-5678", result.getContact());
        assertEquals("Doctor", result.getRole());
        assertEquals("Part-Time", result.getEmploymentStatus());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        assertEquals(200L, result.getFacilityId());
    }

    @Test
    @DisplayName("Test toEntity creates Staff from CreateStaffRequest with null endDate")
    void testToEntityWithNullEndDate() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);

        createRequest.setFirstName("Bob");
        createRequest.setLastName("Johnson");
        createRequest.setContact("555-9999");
        createRequest.setRole("Technician");
        createRequest.setEmploymentStatus("Full-Time");
        createRequest.setStartDate(startDate);
        createRequest.setEndDate(null);

        Staff result = Staff.toEntity(createRequest, 300L);

        assertNotNull(result);
        assertEquals("Bob", result.getFirstName());
        assertEquals("Johnson", result.getLastName());
        assertEquals("555-9999", result.getContact());
        assertEquals("Technician", result.getRole());
        assertEquals("Full-Time", result.getEmploymentStatus());
        assertEquals(startDate, result.getStartDate());
        assertNull(result.getEndDate());
        assertEquals(300L, result.getFacilityId());
    }

    @Test
    @DisplayName("Test toEntity creates Staff from CreateStaffRequest with null contact")
    void testToEntityWithNullContact() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);

        createRequest.setFirstName("Alice");
        createRequest.setLastName("Williams");
        createRequest.setContact(null);
        createRequest.setRole("Administrator");
        createRequest.setEmploymentStatus("Full-Time");
        createRequest.setStartDate(startDate);
        createRequest.setEndDate(null);

        Staff result = Staff.toEntity(createRequest, 400L);

        assertNotNull(result);
        assertEquals("Alice", result.getFirstName());
        assertEquals("Williams", result.getLastName());
        assertNull(result.getContact());
        assertEquals("Administrator", result.getRole());
        assertEquals("Full-Time", result.getEmploymentStatus());
        assertEquals(startDate, result.getStartDate());
        assertEquals(400L, result.getFacilityId());
    }

    @Test
    @DisplayName("Test updateFromRequest updates all non-null fields")
    void testUpdateFromRequestWithAllFields() {
        staff.setFirstName("OldFirst");
        staff.setLastName("OldLast");
        staff.setContact("555-0000");
        staff.setRole("OldRole");
        staff.setEmploymentStatus("OldStatus");
        staff.setStartDate(LocalDate.of(2020, 1, 1));
        staff.setEndDate(LocalDate.of(2021, 1, 1));

        LocalDate newStartDate = LocalDate.of(2023, 6, 1);
        LocalDate newEndDate = LocalDate.of(2024, 6, 1);

        updateRequest.setFirstName("NewFirst");
        updateRequest.setLastName("NewLast");
        updateRequest.setContact("555-1111");
        updateRequest.setRole("NewRole");
        updateRequest.setEmploymentStatus("NewStatus");
        updateRequest.setStartDate(newStartDate);
        updateRequest.setEndDate(newEndDate);

        staff.updateFromRequest(updateRequest);

        assertEquals("NewFirst", staff.getFirstName());
        assertEquals("NewLast", staff.getLastName());
        assertEquals("555-1111", staff.getContact());
        assertEquals("NewRole", staff.getRole());
        assertEquals("NewStatus", staff.getEmploymentStatus());
        assertEquals(newStartDate, staff.getStartDate());
        assertEquals(newEndDate, staff.getEndDate());
    }

    @Test
    @DisplayName("Test updateFromRequest does not update null fields")
    void testUpdateFromRequestWithNullFields() {
        staff.setFirstName("OriginalFirst");
        staff.setLastName("OriginalLast");
        staff.setContact("555-2222");
        staff.setRole("OriginalRole");
        staff.setEmploymentStatus("OriginalStatus");
        staff.setStartDate(LocalDate.of(2022, 1, 1));
        staff.setEndDate(LocalDate.of(2023, 1, 1));

        updateRequest.setFirstName(null);
        updateRequest.setLastName(null);
        updateRequest.setContact(null);
        updateRequest.setRole(null);
        updateRequest.setEmploymentStatus(null);
        updateRequest.setStartDate(null);
        updateRequest.setEndDate(null);

        staff.updateFromRequest(updateRequest);

        assertEquals("OriginalFirst", staff.getFirstName());
        assertEquals("OriginalLast", staff.getLastName());
        assertEquals("555-2222", staff.getContact());
        assertEquals("OriginalRole", staff.getRole());
        assertEquals("OriginalStatus", staff.getEmploymentStatus());
        assertEquals(LocalDate.of(2022, 1, 1), staff.getStartDate());
        assertEquals(LocalDate.of(2023, 1, 1), staff.getEndDate());
    }

    @Test
    @DisplayName("Test updateFromRequest updates only firstName")
    void testUpdateFromRequestOnlyFirstName() {
        staff.setFirstName("OldFirst");
        staff.setLastName("OldLast");
        staff.setContact("555-3333");

        updateRequest.setFirstName("UpdatedFirst");
        updateRequest.setLastName(null);
        updateRequest.setContact(null);

        staff.updateFromRequest(updateRequest);

        assertEquals("UpdatedFirst", staff.getFirstName());
        assertEquals("OldLast", staff.getLastName());
        assertEquals("555-3333", staff.getContact());
    }

    @Test
    @DisplayName("Test updateFromRequest updates only lastName")
    void testUpdateFromRequestOnlyLastName() {
        staff.setFirstName("OldFirst");
        staff.setLastName("OldLast");
        staff.setRole("OldRole");

        updateRequest.setFirstName(null);
        updateRequest.setLastName("UpdatedLast");
        updateRequest.setRole(null);

        staff.updateFromRequest(updateRequest);

        assertEquals("OldFirst", staff.getFirstName());
        assertEquals("UpdatedLast", staff.getLastName());
        assertEquals("OldRole", staff.getRole());
    }

    @Test
    @DisplayName("Test updateFromRequest updates only contact")
    void testUpdateFromRequestOnlyContact() {
        staff.setContact("555-4444");
        staff.setRole("OldRole");

        updateRequest.setContact("555-5555");
        updateRequest.setRole(null);

        staff.updateFromRequest(updateRequest);

        assertEquals("555-5555", staff.getContact());
        assertEquals("OldRole", staff.getRole());
    }

    @Test
    @DisplayName("Test updateFromRequest updates only role")
    void testUpdateFromRequestOnlyRole() {
        staff.setRole("OldRole");
        staff.setEmploymentStatus("OldStatus");

        updateRequest.setRole("UpdatedRole");
        updateRequest.setEmploymentStatus(null);

        staff.updateFromRequest(updateRequest);

        assertEquals("UpdatedRole", staff.getRole());
        assertEquals("OldStatus", staff.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test updateFromRequest updates only employmentStatus")
    void testUpdateFromRequestOnlyEmploymentStatus() {
        staff.setEmploymentStatus("OldStatus");
        staff.setRole("OldRole");

        updateRequest.setEmploymentStatus("UpdatedStatus");
        updateRequest.setRole(null);

        staff.updateFromRequest(updateRequest);

        assertEquals("UpdatedStatus", staff.getEmploymentStatus());
        assertEquals("OldRole", staff.getRole());
    }

    @Test
    @DisplayName("Test updateFromRequest updates only startDate")
    void testUpdateFromRequestOnlyStartDate() {
        staff.setStartDate(LocalDate.of(2022, 1, 1));
        staff.setEndDate(LocalDate.of(2023, 1, 1));

        LocalDate newStartDate = LocalDate.of(2023, 6, 1);
        updateRequest.setStartDate(newStartDate);
        updateRequest.setEndDate(null);

        staff.updateFromRequest(updateRequest);

        assertEquals(newStartDate, staff.getStartDate());
        assertEquals(LocalDate.of(2023, 1, 1), staff.getEndDate());
    }

    @Test
    @DisplayName("Test updateFromRequest updates only endDate")
    void testUpdateFromRequestOnlyEndDate() {
        staff.setStartDate(LocalDate.of(2022, 1, 1));
        staff.setEndDate(LocalDate.of(2023, 1, 1));

        LocalDate newEndDate = LocalDate.of(2024, 12, 31);
        updateRequest.setStartDate(null);
        updateRequest.setEndDate(newEndDate);

        staff.updateFromRequest(updateRequest);

        assertEquals(LocalDate.of(2022, 1, 1), staff.getStartDate());
        assertEquals(newEndDate, staff.getEndDate());
    }

    @Test
    @DisplayName("Test getters and setters for id")
    void testIdGetterSetter() {
        staff.setId(999L);
        assertEquals(999L, staff.getId());
    }

    @Test
    @DisplayName("Test getters and setters for firstName")
    void testFirstNameGetterSetter() {
        staff.setFirstName("TestFirst");
        assertEquals("TestFirst", staff.getFirstName());
    }

    @Test
    @DisplayName("Test getters and setters for lastName")
    void testLastNameGetterSetter() {
        staff.setLastName("TestLast");
        assertEquals("TestLast", staff.getLastName());
    }

    @Test
    @DisplayName("Test getters and setters for contact")
    void testContactGetterSetter() {
        staff.setContact("555-6666");
        assertEquals("555-6666", staff.getContact());
    }

    @Test
    @DisplayName("Test getters and setters for role")
    void testRoleGetterSetter() {
        staff.setRole("TestRole");
        assertEquals("TestRole", staff.getRole());
    }

    @Test
    @DisplayName("Test getters and setters for employmentStatus")
    void testEmploymentStatusGetterSetter() {
        staff.setEmploymentStatus("TestStatus");
        assertEquals("TestStatus", staff.getEmploymentStatus());
    }

    @Test
    @DisplayName("Test getters and setters for startDate")
    void testStartDateGetterSetter() {
        LocalDate date = LocalDate.of(2023, 5, 15);
        staff.setStartDate(date);
        assertEquals(date, staff.getStartDate());
    }

    @Test
    @DisplayName("Test getters and setters for endDate")
    void testEndDateGetterSetter() {
        LocalDate date = LocalDate.of(2024, 5, 15);
        staff.setEndDate(date);
        assertEquals(date, staff.getEndDate());
    }

    @Test
    @DisplayName("Test getters and setters for facilityId")
    void testFacilityIdGetterSetter() {
        staff.setFacilityId(500L);
        assertEquals(500L, staff.getFacilityId());
    }

    @Test
    @DisplayName("Test getters and setters for createdAt")
    void testCreatedAtGetterSetter() {
        LocalDateTime dateTime = LocalDateTime.now();
        staff.setCreatedAt(dateTime);
        assertEquals(dateTime, staff.getCreatedAt());
    }

    @Test
    @DisplayName("Test getters and setters for updatedAt")
    void testUpdatedAtGetterSetter() {
        LocalDateTime dateTime = LocalDateTime.now();
        staff.setUpdatedAt(dateTime);
        assertEquals(dateTime, staff.getUpdatedAt());
    }

    @Test
    @DisplayName("Test getters and setters for createdBy")
    void testCreatedByGetterSetter() {
        staff.setCreatedBy("testUser");
        assertEquals("testUser", staff.getCreatedBy());
    }

    @Test
    @DisplayName("Test getters and setters for updatedBy")
    void testUpdatedByGetterSetter() {
        staff.setUpdatedBy("testUpdater");
        assertEquals("testUpdater", staff.getUpdatedBy());
    }

    @Test
    @DisplayName("Test getters and setters for facility")
    void testFacilityGetterSetter() {
        Facility facility = new Facility();
        staff.setFacility(facility);
        assertEquals(facility, staff.getFacility());
    }

    @Test
    @DisplayName("Test equals returns true for same object")
    void testEqualsSameObject() {
        assertTrue(staff.equals(staff));
    }

    @Test
    @DisplayName("Test equals returns false for null")
    void testEqualsNull() {
        assertFalse(staff.equals(null));
    }

    @Test
    @DisplayName("Test equals returns false for different class")
    void testEqualsDifferentClass() {
        assertFalse(staff.equals("NotAStaff"));
    }

    @Test
    @DisplayName("Test equals returns true for same id")
    void testEqualsSameId() {
        Staff staff1 = new Staff();
        staff1.setId(1L);

        Staff staff2 = new Staff();
        staff2.setId(1L);

        assertTrue(staff1.equals(staff2));
    }

    @Test
    @DisplayName("Test equals returns false for different id")
    void testEqualsDifferentId() {
        Staff staff1 = new Staff();
        staff1.setId(1L);

        Staff staff2 = new Staff();
        staff2.setId(2L);

        assertFalse(staff1.equals(staff2));
    }

    @Test
    @DisplayName("Test equals returns true for both null ids")
    void testEqualsBothNullIds() {
        Staff staff1 = new Staff();
        Staff staff2 = new Staff();

        assertTrue(staff1.equals(staff2));
    }

    @Test
    @DisplayName("Test equals returns false when one id is null")
    void testEqualsOneNullId() {
        Staff staff1 = new Staff();
        staff1.setId(1L);

        Staff staff2 = new Staff();

        assertFalse(staff1.equals(staff2));
    }

    @Test
    @DisplayName("Test hashCode returns same value for same id")
    void testHashCodeSameId() {
        Staff staff1 = new Staff();
        staff1.setId(1L);

        Staff staff2 = new Staff();
        staff2.setId(1L);

        assertEquals(staff1.hashCode(), staff2.hashCode());
    }

    @Test
    @DisplayName("Test hashCode returns different value for different id")
    void testHashCodeDifferentId() {
        Staff staff1 = new Staff();
        staff1.setId(1L);

        Staff staff2 = new Staff();
        staff2.setId(2L);

        assertNotEquals(staff1.hashCode(), staff2.hashCode());
    }

    @Test
    @DisplayName("Test hashCode returns same value for null ids")
    void testHashCodeNullIds() {
        Staff staff1 = new Staff();
        Staff staff2 = new Staff();

        assertEquals(staff1.hashCode(), staff2.hashCode());
    }

    @Test
    @DisplayName("Test toString contains all field values")
    void testToString() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 6, 1, 15, 30);

        staff.setId(1L);
        staff.setFirstName("John");
        staff.setLastName("Doe");
        staff.setContact("555-7777");
        staff.setRole("Nurse");
        staff.setEmploymentStatus("Full-Time");
        staff.setStartDate(startDate);
        staff.setEndDate(endDate);
        staff.setFacilityId(100L);
        staff.setCreatedAt(createdAt);
        staff.setUpdatedAt(updatedAt);
        staff.setCreatedBy("admin");
        staff.setUpdatedBy("admin");

        String result = staff.toString();

        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("firstName='John'"));
        assertTrue(result.contains("lastName='Doe'"));
        assertTrue(result.contains("contact='555-7777'"));
        assertTrue(result.contains("role='Nurse'"));
        assertTrue(result.contains("employmentStatus='Full-Time'"));
        assertTrue(result.contains("startDate=" + startDate));
        assertTrue(result.contains("endDate=" + endDate));
        assertTrue(result.contains("facilityId=100"));
        assertTrue(result.contains("createdAt=" + createdAt));
        assertTrue(result.contains("updatedAt=" + updatedAt));
        assertTrue(result.contains("createdBy='admin'"));
        assertTrue(result.contains("updatedBy='admin'"));
    }

    @Test
    @DisplayName("Test toString with null values")
    void testToStringWithNullValues() {
        String result = staff.toString();

        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("firstName='null'"));
        assertTrue(result.contains("lastName='null'"));
        assertTrue(result.contains("contact='null'"));
    }

    /**
     * Mock CreateStaffRequest class for testing
     */
    static class CreateStaffRequest {
        private String firstName;
        private String lastName;
        private String contact;
        private String role;
        private String employmentStatus;
        private LocalDate startDate;
        private LocalDate endDate;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getContact() { return contact; }
        public void setContact(String contact) { this.contact = contact; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getEmploymentStatus() { return employmentStatus; }
        public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }

    /**
     * Mock UpdateStaffRequest class for testing
     */
    static class UpdateStaffRequest {
        private String firstName;
        private String lastName;
        private String contact;
        private String role;
        private String employmentStatus;
        private LocalDate startDate;
        private LocalDate endDate;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getContact() { return contact; }
        public void setContact(String contact) { this.contact = contact; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getEmploymentStatus() { return employmentStatus; }
        public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }

    /**
     * Mock Facility class for testing
     */
    static class Facility {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }
}