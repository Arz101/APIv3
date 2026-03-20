package com.spring.api.API.Repositories;

import com.spring.api.API.models.Profiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IProfileRepository extends JpaRepository<Profiles, Long> {
    Optional<Profiles> findProfilesByUserUsername(String userUsername);
}
