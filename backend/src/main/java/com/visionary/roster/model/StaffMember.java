package com.visionary.roster.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * JPA Entity representing a staff member in the roster system.
 * This entity enforces multi-tenant facility scoping through the facilityId field,
 * ensuring that staff members are properly isolated by facility.
 */
@Entity
@Table(name = "staff_member")
public class StaffMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contact;

    @Column(nullable = false)
    private String role;

    @Column(name = "employment_status", nullable = false)
    private String employmentStatus;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Facility identifier that enforces multi-tenant facility scoping.
     * This field ensures that staff members are properly associated with
     * their respective facilities for data isolation and security.
     */
    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    /**
     * No-args constructor required by JPA.
     */
    public StaffMember() {
    }

    /**
     * All-args constructor for convenient object creation.
     *
     * @param id               the unique identifier
     * @param name             the staff member's name
     * @param contact          the staff member's contact information
     * @param role             the staff member's role
     * @param employmentStatus the employment status
     * @param startDate        the start date of employment
     * @param endDate          the end date of employment (nullable)
     * @param facilityId       the facility identifier for multi-tenant scoping
     */
    public StaffMember(Long id, String name, String contact, String role, String employmentStatus, 
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffMember that = (StaffMember) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StaffMember{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", contact='" + contact + '\'' +
                ", role='" + role + '\'' +
                ", employmentStatus='" + employmentStatus + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", facilityId=" + facilityId +
                '}';
    }
}