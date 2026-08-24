package com.vis.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_account",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_facility_username",
            columnNames = {"facility_id", "username"}
        )
    },
    indexes = {
        @Index(name = "idx_user_account_facility_role", columnList = "facility_id, role"),
        @Index(name = "idx_user_account_staff_member", columnList = "staff_member_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_account_id")
    private Long userAccountId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_account_facility"))
    private Facility facility;

    @NotNull
    @Size(max = 100)
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @NotNull
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;

    @Column(name = "staff_member_id")
    private Long staffMemberId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum UserRole {
        MANAGER,
        STAFF,
        SUPERVISOR
    }
}