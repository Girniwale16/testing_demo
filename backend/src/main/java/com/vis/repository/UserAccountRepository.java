package com.vis.repository;

import com.vis.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    
    Optional<UserAccount> findByFacilityFacilityIdAndUsername(Long facilityId, String username);
    
    boolean existsByFacilityFacilityIdAndUsername(Long facilityId, String username);
}