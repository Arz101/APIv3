package com.spring.api.API.Repositories;

import com.spring.api.API.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u.id FROM User u WHERE u.username = :username")
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String Email);

    @Query("""
        SELECT u.id
        FROM User u
        WHERE u.username =:username
    """)
    Optional<Long> getIdByUsername(@Param("username") String username);
}
