package com.visionary.roster.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "employment_status", nullable = false, length = 20)
    private String employmentStatus = "ACTIVE";

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
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
     * Gets the unique identifier of the staff member.
     *
     * @return the staff id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the staff member.
     *
     * @param id the staff id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the first name of the staff member.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the staff member.
     *
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name of the staff member.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the staff member.
     *
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the email address of the staff member.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the staff member.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the role of the staff member.
     *
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role of the staff member.
     *
     * @param role the role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Gets the employment status of the staff member.
     *
     * @return the employment status
     */
    public String getEmploymentStatus() {
        return employmentStatus;
    }

    /**
     * Sets the employment status of the staff member.
     *
     * @param employmentStatus the employment status
     */
    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    /**
     * Gets the end date of employment.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of employment.
     *
     * @param endDate the end date
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Gets the facility where the staff member works.
     *
     * @return the facility
     */
    public Facility getFacility() {
        return facility;
    }

    /**
     * Sets the facility where the staff member works.
     *
     * @param facility the facility
     */
    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    /**
     * Gets the timestamp when the staff record was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the staff record was created.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the staff record was last updated.
     *
     * @return the update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when the staff record was last updated.
     *
     * @param updatedAt the update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Checks if the staff member is currently active.
     *
     * @return true if employment status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return "ACTIVE".equals(employmentStatus);
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
}