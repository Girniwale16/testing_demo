package com.visionary.roster.model;

import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.UpdateStaffRequest;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity representing a Staff member in the roster system.
 * Maps to the "staff" table in the database.
 */
@Entity
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "contact", length = 50)
    private String contact;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "employment_status", nullable = false, length = 20)
    private String employmentStatus = "ACTIVE";

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", insertable = false, updatable = false)
    private Facility facility;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Default constructor
     */
    public Staff() {
    }

    /**
     * Static factory method to create a Staff entity from a CreateStaffRequest.
     *
     * @param request the create staff request
     * @param facilityId the facility the staff member belongs to
     * @return a new Staff instance populated from the request
     */
    public static Staff toEntity(CreateStaffRequest request, Long facilityId) {
        Staff staff = new Staff();
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setEmail(request.getEmail());
        staff.setContact(request.getContact());
        staff.setRole(request.getRole());
        staff.setEmploymentStatus(request.getEmploymentStatus() != null ? request.getEmploymentStatus() : "ACTIVE");
        staff.setStartDate(request.getStartDate());
        staff.setEndDate(request.getEndDate());
        staff.setFacilityId(facilityId);
        return staff;
    }

    /**
     * Updates this staff entity from an UpdateStaffRequest.
     * Only non-null fields on the request are applied.
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

    /**
     * Lifecycle callback that updates the updatedAt timestamp before entity update.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
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

    /**
     * Deactivates the staff member by setting employment status to INACTIVE
     * and recording the end date.
     *
     * @param endDate the date when employment ends
     */
    public void deactivate(LocalDate endDate) {
        this.employmentStatus = "INACTIVE";
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Staff that = (Staff) o;
        return Objects.equals(id, that.id);
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
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", employmentStatus='" + employmentStatus + '\'' +
                ", active=" + active +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", facilityId=" + facilityId +
                '}';
    }
}