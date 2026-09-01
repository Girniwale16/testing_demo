package com.visionary.roster.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity class representing a Staff Member in the roster system.
 * 
 * @author Visionary Roster Team
 * @version 1.0
 */
@Entity
@Table(name = "staff_members")
public class StaffMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Default constructor
     */
    public StaffMember() {
    }

    /**
     * Constructor with parameters
     * 
     * @param firstName the first name of the staff member
     * @param lastName the last name of the staff member
     * @param email the email address of the staff member
     */
    public StaffMember(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.active = true;
    }

    /**
     * Gets the ID of the staff member
     * 
     * @return the staff member ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the staff member
     * 
     * @param id the staff member ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the first name of the staff member
     * 
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the staff member
     * 
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name of the staff member
     * 
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the staff member
     * 
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the email address of the staff member
     * 
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the staff member
     * 
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Checks if the staff member is active
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active status of the staff member
     * 
     * @param active the active status
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "StaffMember{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StaffMember that = (StaffMember) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}