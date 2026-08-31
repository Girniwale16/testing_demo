package com.visionary.roster.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "facility")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facility_id")
    private Long facilityId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<Staff> staffMembers = new HashSet<>();

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

    public Set<Staff> getStaffMembers() {
        return Collections.unmodifiableSet(staffMembers);
    }

    public void addStaff(Staff staff) {
        staffMembers.add(staff);
        staff.setFacility(this);
    }

    public void removeStaff(Staff staff) {
        staffMembers.remove(staff);
        staff.setFacility(null);
    }
}