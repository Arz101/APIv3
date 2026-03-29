package com.spring.api.API.services;

import com.spring.api.API.Repositories.IEmail_TokensRepository;
import com.spring.api.API.Repositories.ITokensRepository;
import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.Email_Tokens;
import com.spring.api.API.models.Tokens;
import com.spring.api.API.models.User;
import com.spring.api.API.security.Exceptions.InvalidTokenException;
import org.springframework.transaction.annotation.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.UUID;


@Service
public class TokenService {
    private final ITokensRepository repository;
    private final IUserRepository userRepository;
    private final IEmail_TokensRepository email_TokensRepository;

    public TokenService(ITokensRepository repository,
                        IUserRepository userRepository,
                        IEmail_TokensRepository email_TokensRepository){
        this.repository = repository;
        this.userRepository = userRepository;
        this.email_TokensRepository = email_TokensRepository;
    }

    @Transactional
    public String create(String username){
        String token;
        long start = System.currentTimeMillis();
        User user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));        

        if(this.repository.existsAnActiveToken(user)){
            Tokens currentToken = this.repository.getCurrectTokenByUser(user);
            currentToken.setRevoked(true);
            this.repository.saveAndFlush(currentToken);
        }

        token = UUID.randomUUID().toString();

        Tokens new_token = new Tokens();
        new_token.setTokenHash(token);
        new_token.setAssignedTo(user);
        new_token.setExpireAt(OffsetDateTime.now().plusDays(7));
        new_token.setTokenType((short) 3);
        new_token.setRevoked(false);

        this.repository.save(new_token);
        System.out.println("REVOKE AND INSERT NEW TOKEN: " + (System.currentTimeMillis() - start));
        return token;
    }

    @Transactional(readOnly = true)
    public String validate_refresh_token(String token_hash){
        Tokens token = this.repository.findByTokenHashAndRevoked(token_hash, false)
                .orElseThrow(() -> new InvalidTokenException("Invalid Token"));

        if(token.getExpireAt().isBefore(OffsetDateTime.now()) || token.getRevoked()) {
            if (!token.getRevoked())
                this.revokeTokens(token);
            throw new InvalidTokenException("Token was expired or revoked");
        }
        return token.getAssignedTo().getUsername();
    }

    public void revokeTokens(@NonNull Tokens token){
        token.setRevoked(true);
        this.repository.save(token);
    }

    @Transactional
    public String saveAccountTokens(User user){
        String token = UUID.randomUUID().toString();
        
        Email_Tokens new_token = new Email_Tokens();
        new_token.setToken_hash(token);
        new_token.setAssignedTo(user);
        new_token.setExpire_at(OffsetDateTime.now().plusMinutes(15));
        new_token.setTokenType((short) 1);
        new_token.setUsed(false);

        this.email_TokensRepository.save(new_token);
        return token;
    }

    public void validateEmailToken(String token_hash){
        Email_Tokens token = this.email_TokensRepository.findToken(token_hash);
        if(token == null || token.isUsed() || token.getExpire_at().isBefore(OffsetDateTime.now()))
            throw new InvalidTokenException("Invalid or expired token");

        User user = token.getAssignedTo();
        if (user == null)
            throw new InvalidTokenException("Invalid token");

        user.setStatus("active");
        this.userRepository.save(user);

        token.setUsed(true);
        this.email_TokensRepository.save(token);
    }
}

