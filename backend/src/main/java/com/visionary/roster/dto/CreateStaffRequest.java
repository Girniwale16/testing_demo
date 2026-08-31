package com.visionary.roster.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import com.visionary.roster.entity.StaffMember;

/**
 * Data Transfer Object for creating new staff members.
 * 
 * This DTO is used to capture staff creation requests from the client.
 * The facilityId is injected by the service layer from the authenticated user context.
 * Date validation (end_date >= start_date) is performed in the service layer, not in this DTO.
 */
public class CreateStaffRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Contact is required")
    private String contact;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Employment status is required")
    private String employmentStatus;

    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * No-args constructor.
     */
    public CreateStaffRequest() {
    }

    /**
     * All-args constructor.
     *
     * @param name the staff member's name
     * @param contact the staff member's contact information
     * @param role the staff member's role
     * @param employmentStatus the staff member's employment status
     * @param startDate the staff member's start date (optional)
     * @param endDate the staff member's end date (optional)
     */
    public CreateStaffRequest(String name, String contact, String role, String employmentStatus, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.contact = contact;
        this.role = role;
        this.employmentStatus = employmentStatus;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Converts this DTO to a StaffMember entity.
     * 
     * The facilityId is injected by the service layer from the authenticated user context.
     * Date validation (end_date >= start_date) is performed in the service layer, not in this DTO.
     *
     * @param facilityId the facility ID to associate with the staff member
     * @return a new StaffMember entity populated with data from this DTO
     */
    public StaffMember toEntity(Long facilityId) {
        StaffMember staffMember = new StaffMember();
        staffMember.setFacilityId(facilityId);
        staffMember.setName(this.name);
        staffMember.setContact(this.contact);
        staffMember.setRole(this.role);
        staffMember.setEmploymentStatus(this.employmentStatus);
        staffMember.setStartDate(this.startDate);
        staffMember.setEndDate(this.endDate);
        return staffMember;
    }
}