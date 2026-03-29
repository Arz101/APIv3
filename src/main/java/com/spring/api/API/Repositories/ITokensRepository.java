package com.spring.api.API.Repositories;

import com.spring.api.API.models.Tokens;
import com.spring.api.API.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ITokensRepository extends JpaRepository<Tokens, Long> {
    Optional<Tokens> findByTokenHashAndRevoked(String tokenHash, Boolean revoked);
    Optional<Tokens> findTokensByAssignedToAndRevoked(User assignedTo, Boolean revoked);

    @Modifying
    @Query("""
        UPDATE Tokens t 
        SET t.revoked = true 
        WHERE t.assignedTo = :user AND t.revoked = false
    """)
    void revokeAllByUser(@Param("user") User user);

    @Query("""
        SELECT t
        FROM Tokens t
        WHERE t.assignedTo = :user AND t.revoked = false
    """)
    Tokens getCurrectTokenByUser(@Param("user") User user);

    @Query("""
        SELECT COUNT(*) >= 1
        FROM Tokens t
        WHERE t.assignedTo =:user AND t.revoked = false
    """)
    Boolean existsAnActiveToken(@Param("user") User user);
}
