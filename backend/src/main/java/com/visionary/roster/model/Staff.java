package com.visionary.roster.model;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Staff entity class representing staff members in the roster system.
 * Maps to the 'staff' table in the database.
 */
@Entity
@Table(name = "staff")
@EntityListeners(AuditingEntityListener.class)
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "contact", length = 50)
    private String contact;

    @Column(name = "role", nullable = false, length = 100)
    private String role;

    @Column(name = "employment_status", nullable = false, length = 50)
    private String employmentStatus;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @ManyToOne
    @JoinColumn(name = "facility_id", insertable = false, updatable = false)
    private Facility facility;

    /**
     * No-args constructor required by JPA.
     */
    public Staff() {
    }

    /**
     * All-args constructor.
     */
    public Staff(Long id, String firstName, String lastName, String contact, String role, 
                 String employmentStatus, LocalDate startDate, LocalDate endDate, Long facilityId, 
                 LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy, String updatedBy) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contact = contact;
        this.role = role;
        this.employmentStatus = employmentStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.facilityId = facilityId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    /**
     * Static factory method to create Staff entity from CreateStaffRequest.
     *
     * @param request the create staff request
     * @param facilityId the facility ID
     * @return new Staff instance
     */
    public static Staff toEntity(CreateStaffRequest request, Long facilityId) {
        Staff staff = new Staff();
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setContact(request.getContact());
        staff.setRole(request.getRole());
        staff.setEmploymentStatus(request.getEmploymentStatus());
        staff.setStartDate(request.getStartDate());
        staff.setEndDate(request.getEndDate());
        staff.setFacilityId(facilityId);
        return staff;
    }

    /**
     * Updates the staff entity from UpdateStaffRequest.
     * Only updates non-null fields from the request.
     *
     * @param request the update staff request
     */
    public void updateFromRequest(UpdateStaffRequest request) {
        if (request.getFirstName() != null) {
            this.firstName = request.getFirstName();
        }
        if (request.getLastName() != null) {
            this.lastName = request.getLastName();
        }
        if (request.getContact() != null) {
            this.contact = request.getContact();
        }
        if (request.getRole() != null) {
            this.role = request.getRole();
        }
        if (request.getEmploymentStatus() != null) {
            this.employmentStatus = request.getEmploymentStatus();
        }
        if (request.getStartDate() != null) {
            this.startDate = request.getStartDate();
        }
        if (request.getEndDate() != null) {
            this.endDate = request.getEndDate();
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Staff staff = (Staff) o;
        return Objects.equals(id, staff.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Staff{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", contact='" + contact + '\'' +
                ", role='" + role + '\'' +
                ", employmentStatus='" + employmentStatus + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", facilityId=" + facilityId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}