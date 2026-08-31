package com.visionary.roster;

import com.visionary.roster.model.Facility;
import com.visionary.roster.model.Staff;
import com.visionary.roster.model.UserAccount;
import com.visionary.roster.repository.FacilityRepository;
import com.visionary.roster.repository.StaffRepository;
import com.visionary.roster.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StaffControllerIntegrationTestEnhanced {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private Facility facilityA;
    private Facility facilityB;
    private Staff testStaff;
    private UserAccount managerUser;
    private UserAccount nonManagerUser;
    private UserAccount managerUserFacilityB;

    @BeforeEach
    void setUp() {
        facilityA = new Facility();
        facilityA.setFacilityName("Facility A");
        facilityA = facilityRepository.save(facilityA);

        facilityB = new Facility();
        facilityB.setFacilityName("Facility B");
        facilityB = facilityRepository.save(facilityB);

        testStaff = new Staff();
        testStaff.setFirstName("John");
        testStaff.setLastName("Doe");
        testStaff.setEmail("john.doe@example.com");
        testStaff.setPhoneNumber("1234567890");
        testStaff.setRole("NURSE");
        testStaff.setFacility(facilityA);
        testStaff.setActive(true);
        testStaff = staffRepository.save(testStaff);

        managerUser = new UserAccount();
        managerUser.setUsername("manager");
        managerUser.setRole("MANAGER");
        managerUser.setFacility(facilityA);
        managerUser = userAccountRepository.save(managerUser);

        nonManagerUser = new UserAccount();
        nonManagerUser.setUsername("staff");
        nonManagerUser.setRole("STAFF");
        nonManagerUser.setFacility(facilityA);
        nonManagerUser = userAccountRepository.save(nonManagerUser);

        managerUserFacilityB = new UserAccount();
        managerUserFacilityB.setUsername("managerB");
        managerUserFacilityB.setRole("MANAGER");
        managerUserFacilityB.setFacility(facilityB);
        managerUserFacilityB = userAccountRepository.save(managerUserFacilityB);
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testUpdateStaff_Success_VerifyAllFieldsUpdated() throws Exception {
        String updateJson = String.format(
            "{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"email\":\"jane.smith@example.com\",\"phoneNumber\":\"9876543210\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("9876543210"))
                .andExpect(jsonPath("$.role").value("DOCTOR"));

        Staff updatedStaff = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertEquals("Jane", updatedStaff.getFirstName());
        assertEquals("Smith", updatedStaff.getLastName());
        assertEquals("jane.smith@example.com", updatedStaff.getEmail());
        assertEquals("9876543210", updatedStaff.getPhoneNumber());
        assertEquals("DOCTOR", updatedStaff.getRole());
        assertTrue(updatedStaff.isActive());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testUpdateStaff_Success_PartialUpdate() throws Exception {
        String updateJson = String.format(
            "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.updated@example.com\",\"phoneNumber\":\"1234567890\",\"role\":\"NURSE\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        Staff updatedStaff = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertEquals("john.updated@example.com", updatedStaff.getEmail());
    }

    @Test
    @Transactional
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testUpdateStaff_Forbidden_NonManager_VerifyNoChanges() throws Exception {
        String originalEmail = testStaff.getEmail();
        String updateJson = String.format(
            "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane.doe@example.com\",\"phoneNumber\":\"0987654321\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isForbidden());

        Staff unchangedStaff = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertEquals(originalEmail, unchangedStaff.getEmail());
        assertEquals("John", unchangedStaff.getFirstName());
    }

    @Test
    @Transactional
    @WithMockUser(username = "managerB", roles = {"MANAGER"})
    void testUpdateStaff_Forbidden_CrossFacility_VerifyNoChanges() throws Exception {
        String originalEmail = testStaff.getEmail();
        String updateJson = String.format(
            "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane.doe@example.com\",\"phoneNumber\":\"0987654321\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isForbidden());

        Staff unchangedStaff = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertEquals(originalEmail, unchangedStaff.getEmail());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testUpdateStaff_NotFound_WithValidData() throws Exception {
        Long nonExistentStaffId = 99999L;
        String updateJson = String.format(
            "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane.doe@example.com\",\"phoneNumber\":\"0987654321\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + nonExistentStaffId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testUpdateStaff_ValidationError_InvalidEmail() throws Exception {
        String updateJson = String.format(
            "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"invalid-email\",\"phoneNumber\":\"0987654321\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testUpdateStaff_ValidationError_MissingEmail() throws Exception {
        String updateJson = String.format(
            "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"phoneNumber\":\"0987654321\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testUpdateStaff_ValidationError_EmptyFirstName() throws Exception {
        String updateJson = String.format(
            "{\"firstName\":\"\",\"lastName\":\"Doe\",\"email\":\"jane.doe@example.com\",\"phoneNumber\":\"0987654321\",\"role\":\"DOCTOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testDeactivateStaff_Success_VerifyDatabaseState() throws Exception {
        assertTrue(testStaff.isActive());

        mockMvc.perform(post("/api/staff/" + testStaff.getStaffId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Staff deactivatedStaff = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertFalse(deactivatedStaff.isActive());
        assertEquals("John", deactivatedStaff.getFirstName());
        assertEquals("Doe", deactivatedStaff.getLastName());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testDeactivateStaff_Idempotent_VerifyMultipleCalls() throws Exception {
        mockMvc.perform(post("/api/staff/" + testStaff.getStaffId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Staff firstDeactivation = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertFalse(firstDeactivation.isActive());

        mockMvc.perform(post("/api/staff/" + testStaff.getStaffId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Staff secondDeactivation = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertFalse(secondDeactivation.isActive());
    }

    @Test
    @Transactional
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testDeactivateStaff_Forbidden_NonManager_VerifyStillActive() throws Exception {
        assertTrue(testStaff.isActive());

        mockMvc.perform(post("/api/staff/" + testStaff.getStaffId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        Staff unchangedStaff = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertTrue(unchangedStaff.isActive());
    }

    @Test
    @Transactional
    @WithMockUser(username = "managerB", roles = {"MANAGER"})
    void testDeactivateStaff_Forbidden_CrossFacility() throws Exception {
        mockMvc.perform(post("/api/staff/" + testStaff.getStaffId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        Staff unchangedStaff = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertTrue(unchangedStaff.isActive());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testDeactivateStaff_NotFound() throws Exception {
        Long nonExistentStaffId = 99999L;

        mockMvc.perform(post("/api/staff/" + nonExistentStaffId + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testListActiveStaff_ExcludesDeactivated_VerifyCount() throws Exception {
        Staff activeStaff1 = new Staff();
        activeStaff1.setFirstName("Active1");
        activeStaff1.setLastName("Staff");
        activeStaff1.setEmail("active1@example.com");
        activeStaff1.setPhoneNumber("1111111111");
        activeStaff1.setRole("NURSE");
        activeStaff1.setFacility(facilityA);
        activeStaff1.setActive(true);
        staffRepository.save(activeStaff1);

        Staff activeStaff2 = new Staff();
        activeStaff2.setFirstName("Active2");
        activeStaff2.setLastName("Staff");
        activeStaff2.setEmail("active2@example.com");
        activeStaff2.setPhoneNumber("2222222222");
        activeStaff2.setRole("DOCTOR");
        activeStaff2.setFacility(facilityA);
        activeStaff2.setActive(true);
        staffRepository.save(activeStaff2);

        Staff deactivatedStaff1 = new Staff();
        deactivatedStaff1.setFirstName("Deactivated1");
        deactivatedStaff1.setLastName("Staff");
        deactivatedStaff1.setEmail("deactivated1@example.com");
        deactivatedStaff1.setPhoneNumber("3333333333");
        deactivatedStaff1.setRole("NURSE");
        deactivatedStaff1.setFacility(facilityA);
        deactivatedStaff1.setActive(false);
        staffRepository.save(deactivatedStaff1);

        Staff deactivatedStaff2 = new Staff();
        deactivatedStaff2.setFirstName("Deactivated2");
        deactivatedStaff2.setLastName("Staff");
        deactivatedStaff2.setEmail("deactivated2@example.com");
        deactivatedStaff2.setPhoneNumber("4444444444");
        deactivatedStaff2.setRole("DOCTOR");
        deactivatedStaff2.setFacility(facilityA);
        deactivatedStaff2.setActive(false);
        staffRepository.save(deactivatedStaff2);

        mockMvc.perform(get("/api/staff")
                .param("facilityId", facilityA.getFacilityId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'active1@example.com')]").exists())
                .andExpect(jsonPath("$[?(@.email == 'active2@example.com')]").exists())
                .andExpect(jsonPath("$[?(@.email == 'john.doe@example.com')]").exists())
                .andExpect(jsonPath("$[?(@.email == 'deactivated1@example.com')]").doesNotExist())
                .andExpect(jsonPath("$[?(@.email == 'deactivated2@example.com')]").doesNotExist());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testListActiveStaff_OnlyActiveFacilityA() throws Exception {
        Staff staffFacilityB = new Staff();
        staffFacilityB.setFirstName("Bob");
        staffFacilityB.setLastName("Builder");
        staffFacilityB.setEmail("bob@example.com");
        staffFacilityB.setPhoneNumber("5555555555");
        staffFacilityB.setRole("NURSE");
        staffFacilityB.setFacility(facilityB);
        staffFacilityB.setActive(true);
        staffRepository.save(staffFacilityB);

        mockMvc.perform(get("/api/staff")
                .param("facilityId", facilityA.getFacilityId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'john.doe@example.com')]").exists())
                .andExpect(jsonPath("$[?(@.email == 'bob@example.com')]").doesNotExist());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testListActiveStaff_EmptyResult_NoActiveStaff() throws Exception {
        testStaff.setActive(false);
        staffRepository.save(testStaff);

        mockMvc.perform(get("/api/staff")
                .param("facilityId", facilityA.getFacilityId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testListActiveStaff_EmptyResult_NonExistentFacility() throws Exception {
        Long nonExistentFacilityId = 99999L;

        mockMvc.perform(get("/api/staff")
                .param("facilityId", nonExistentFacilityId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testUpdateStaff_Success_RoleChange() throws Exception {
        String updateJson = String.format(
            "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\",\"phoneNumber\":\"1234567890\",\"role\":\"SUPERVISOR\",\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SUPERVISOR"));

        Staff updatedStaff = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertEquals("SUPERVISOR", updatedStaff.getRole());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testDeactivateStaff_AlreadyDeactivated_Idempotent() throws Exception {
        testStaff.setActive(false);
        staffRepository.save(testStaff);

        mockMvc.perform(post("/api/staff/" + testStaff.getStaffId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Staff stillDeactivated = staffRepository.findById(testStaff.getStaffId()).orElseThrow();
        assertFalse(stillDeactivated.isActive());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testUpdateStaff_ValidationError_NullFields() throws Exception {
        String updateJson = String.format(
            "{\"facilityId\":%d}",
            facilityA.getFacilityId()
        );

        mockMvc.perform(put("/api/staff/" + testStaff.getStaffId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Transactional
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testListActiveStaff_MultipleRoles() throws Exception {
        Staff nurseStaff = new Staff();
        nurseStaff.setFirstName("Nurse");
        nurseStaff.setLastName("One");
        nurseStaff.setEmail("nurse1@example.com");
        nurseStaff.setPhoneNumber("6666666666");
        nurseStaff.setRole("NURSE");
        nurseStaff.setFacility(facilityA);
        nurseStaff.setActive(true);
        staffRepository.save(nurseStaff);

        Staff doctorStaff = new Staff();
        doctorStaff.setFirstName("Doctor");
        doctorStaff.setLastName("One");
        doctorStaff.setEmail("doctor1@example.com");
        doctorStaff.setPhoneNumber("7777777777");
        doctorStaff.setRole("DOCTOR");
        doctorStaff.setFacility(facilityA);
        doctorStaff.setActive(true);
        staffRepository.save(doctorStaff);

        mockMvc.perform(get("/api/staff")
                .param("facilityId", facilityA.getFacilityId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.role == 'NURSE')]").exists())
                .andExpect(jsonPath("$[?(@.role == 'DOCTOR')]").exists());
    }
}