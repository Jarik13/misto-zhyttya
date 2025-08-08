package org.example.userprofileservice.repository;

import org.example.userprofileservice.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<Profile, UUID> {
    boolean existsByPhoneNumber(String phoneNumber);
}
