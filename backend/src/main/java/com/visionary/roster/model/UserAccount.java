package com.visionary.roster.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * UserAccount entity represents user authentication and authorization credentials.
 * 
 * IMPORTANT DISTINCTION:
 * - UserAccount deactivation (accountStatus/accountEndDate): Controls authentication and authorization access.
 *   When a user account is deactivated, the user cannot log in or access the system.
 * 
 * - Staff deactivation (employment_status/end_date in Staff entity): Controls employment status.
 *   A staff member's employment may end, but their user account may remain active for historical access,
 *   or vice versa - a user may have an account without being a staff member (e.g., external auditors, contractors).
 * 
 * These two concepts are independent and must be managed separately to support various business scenarios.
 */
@Entity
@Table(name = "user_account", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_facility_username", 
                           columnNames = {"facility_id", "username"})
       },
       indexes = {
           @Index(name = "idx_facility_role", columnList = "facility_id, role"),
           @Index(name = "idx_staff_member", columnList = "staff_member_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "passwordHash")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_account_id")
    private Long userAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_user_account_facility"))
    @NotNull
    private Facility facility;

    @Column(name = "username", nullable = false, length = 100)
    @NotNull
    private String username;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    @NotNull
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 50)
    @NotNull
    private String role;

    @Column(name = "staff_member_id")
    private Long staffMemberId;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;

    @Column(name = "account_status", length = 20)
    private String accountStatus;

    @Column(name = "account_end_date")
    private LocalDate accountEndDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (createdBy == null) {
            createdBy = "system";
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to determine if the user account is active for authentication/authorization.
     * 
     * @return true if the account is active (accountStatus is null or "ACTIVE"), false otherwise
     */
    public boolean isAccountActive() {
        return accountStatus == null || accountStatus.equals("ACTIVE");
    }
}