package com.spring.api.API.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.api.API.models.EmailTokens;


@Repository
public interface IEmailTokensRepository extends JpaRepository<EmailTokens, Long> {
    @Query("SELECT t FROM EmailTokens t WHERE t.tokenHash = :token")
    EmailTokens findToken(@Param("token") String token);
}
