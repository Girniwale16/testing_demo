package com.visionary.roster.dto;

import com.visionary.roster.entity.StaffMember;
import java.time.LocalDate;

/**
 * Data Transfer Object representing the API response payload for staff member data.
 * This DTO is used to transfer staff member information from the backend to the client.
 * It follows the DTO structure pattern established by SessionResponse and LoginResponse for consistency.
 */
public class StaffResponse {

    private Long id;
    private String name;
    private String contact;
    private String role;
    private String employmentStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long facilityId;

    /**
     * No-args constructor
     */
    public StaffResponse() {
    }

    /**
     * All-args constructor
     *
     * @param id the staff member ID
     * @param name the staff member name
     * @param contact the staff member contact information
     * @param role the staff member role
     * @param employmentStatus the employment status
     * @param startDate the start date of employment
     * @param endDate the end date of employment
     * @param facilityId the facility ID associated with the staff member
     */
    public StaffResponse(Long id, String name, String contact, String role, String employmentStatus, 
                        LocalDate startDate, LocalDate endDate, Long facilityId) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.role = role;
        this.employmentStatus = employmentStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.facilityId = facilityId;
    }

    /**
     * Static factory method to create a StaffResponse from a StaffMember entity.
     * Handles null startDate and endDate gracefully.
     *
     * @param entity the StaffMember entity to convert
     * @return a new StaffResponse instance populated with data from the entity
     */
    public static StaffResponse fromEntity(StaffMember entity) {
        if (entity == null) {
            return null;
        }
        
        StaffResponse response = new StaffResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setContact(entity.getContact());
        response.setRole(entity.getRole());
        response.setEmploymentStatus(entity.getEmploymentStatus());
        response.setStartDate(entity.getStartDate() != null ? entity.getStartDate() : null);
        response.setEndDate(entity.getEndDate() != null ? entity.getEndDate() : null);
        response.setFacilityId(entity.getFacilityId());
        
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }
}