package com.visionary.roster.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing staff members with soft-delete support via employment_status and end_date fields
 */
@Entity
@Table(name = "staff_member")
public class StaffMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_member_id")
    private Long staffMemberId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "contact", nullable = false, length = 255)
    private String contact;

    @Column(name = "role", nullable = false, length = 100)
    private String role;

    // Valid values: ACTIVE, INACTIVE, TERMINATED (enforced by database CHECK constraint)
    @Column(name = "employment_status", nullable = false, length = 20)
    private String employmentStatus;

    // Foreign key reference to facility.facility_id
    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "start_date")
    private LocalDate startDate;

    // Populated when employment_status transitions to INACTIVE or TERMINATED (soft-delete pattern)
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * No-argument constructor (required by JPA)
     */
    public StaffMember() {
    }

    /**
     * All-arguments constructor for convenience
     */
    public StaffMember(Long staffMemberId, String name, String contact, String role, String employmentStatus, 
                       Long facilityId, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt, 
                       LocalDateTime updatedAt) {
        this.staffMemberId = staffMemberId;
        this.name = name;
        this.contact = contact;
        this.role = role;
        this.employmentStatus = employmentStatus;
        this.facilityId = facilityId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getStaffMemberId() {
        return staffMemberId;
    }

    public void setStaffMemberId(Long staffMemberId) {
        this.staffMemberId = staffMemberId;
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

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffMember that = (StaffMember) o;
        return Objects.equals(staffMemberId, that.staffMemberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staffMemberId);
    }

    @Override
    public String toString() {
        return "StaffMember{" +
                "staffMemberId=" + staffMemberId +
                ", name='" + name + '\'' +
                ", contact='" + contact + '\'' +
                ", role='" + role + '\'' +
                ", employmentStatus='" + employmentStatus + '\'' +
                ", facilityId=" + facilityId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}