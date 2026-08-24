package com.visionary.roster.repository;

import com.visionary.roster.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    @Query("SELECT u FROM UserAccount u JOIN FETCH u.facility WHERE u.username = :username AND u.facility.facilityId = :facilityId")
    Optional<UserAccount> findByUsernameAndFacilityId(@Param("username") String username, 
                                                       @Param("facilityId") Long facilityId);

    @Query("SELECT u FROM UserAccount u JOIN FETCH u.facility WHERE u.username = :username")
    Optional<UserAccount> findByUsername(@Param("username") String username);
}