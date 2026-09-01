package com.visionary.roster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Data Transfer Object for updating staff information.
 * Contains validation annotations to ensure data integrity.
 * Start/end date ordering is validated in StaffService, not here.
 */
public class UpdateStaffRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Contact is required")
    @Pattern(regexp = "^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\+?[0-9]{10,15})$",
             message = "Contact must be a valid email or phone number")
    private String contact;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Employment status is required")
    private String employmentStatus;

    @PastOrPresent(message = "Start date must be in the past or present")
    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * No-args constructor.
     */
    public UpdateStaffRequest() {
    }

    /**
     * All-args constructor.
     *
     * @param firstName        the first name
     * @param lastName         the last name
     * @param contact          the contact information
     * @param role             the role
     * @param employmentStatus the employment status
     * @param startDate        the start date
     * @param endDate          the end date
     */
    public UpdateStaffRequest(String firstName, String lastName, String contact, String role,
                              String employmentStatus, LocalDate startDate, LocalDate endDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.contact = contact;
        this.role = role;
        this.employmentStatus = employmentStatus;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateStaffRequest that = (UpdateStaffRequest) o;
        return Objects.equals(firstName, that.firstName) &&
               Objects.equals(lastName, that.lastName) &&
               Objects.equals(contact, that.contact) &&
               Objects.equals(role, that.role) &&
               Objects.equals(employmentStatus, that.employmentStatus) &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, contact, role, employmentStatus, startDate, endDate);
    }

    @Override
    public String toString() {
        return "UpdateStaffRequest{" +
               "firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", contact='" + contact + '\'' +
               ", role='" + role + '\'' +
               ", employmentStatus='" + employmentStatus + '\'' +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               '}';
    }
}
