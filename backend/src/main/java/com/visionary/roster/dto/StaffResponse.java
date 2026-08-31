package com.visionary.roster.dto;

import com.visionary.roster.entity.Staff;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Staff entity responses.
 * Used to transfer staff data to the client layer.
 */
public class StaffResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String employmentStatus;
    private Long facilityId;
    private String facilityName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate endDate;

    /**
     * Default constructor.
     */
    public StaffResponse() {
    }

    /**
     * Gets the staff ID.
     *
     * @return the staff ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the staff ID.
     *
     * @param id the staff ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     *
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name.
     *
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the role.
     *
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role.
     *
     * @param role the role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Gets the employment status.
     *
     * @return the employment status
     */
    public String getEmploymentStatus() {
        return employmentStatus;
    }

    /**
     * Sets the employment status.
     *
     * @param employmentStatus the employment status
     */
    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    /**
     * Gets the facility ID.
     *
     * @return the facility ID
     */
    public Long getFacilityId() {
        return facilityId;
    }

    /**
     * Sets the facility ID.
     *
     * @param facilityId the facility ID
     */
    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    /**
     * Gets the facility name.
     *
     * @return the facility name
     */
    public String getFacilityName() {
        return facilityName;
    }

    /**
     * Sets the facility name.
     *
     * @param facilityName the facility name
     */
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update timestamp.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt the last update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Gets the end date.
     *
     * @return the end date (nullable)
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date.
     *
     * @param endDate the end date (nullable)
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Factory method to create a StaffResponse from a Staff entity.
     *
     * @param staff the Staff entity
     * @return a StaffResponse populated with data from the Staff entity
     */
    public static StaffResponse fromEntity(Staff staff) {
        if (staff == null) {
            return null;
        }

        StaffResponse response = new StaffResponse();
        response.setId(staff.getId());
        response.setFirstName(staff.getFirstName());
        response.setLastName(staff.getLastName());
        response.setEmail(staff.getEmail());
        response.setRole(staff.getRole());
        response.setEmploymentStatus(staff.getEmploymentStatus());
        response.setCreatedAt(staff.getCreatedAt());
        response.setUpdatedAt(staff.getUpdatedAt());
        response.setEndDate(staff.getEndDate());

        if (staff.getFacility() != null) {
            response.setFacilityId(staff.getFacility().getId());
            response.setFacilityName(staff.getFacility().getName());
        }

        return response;
    }
}