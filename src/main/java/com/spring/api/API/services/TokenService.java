package com.spring.api.API.services;

import com.spring.api.API.Repositories.IEmailTokensRepository;
import com.spring.api.API.Repositories.ITokensRepository;
import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.EmailTokens;
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
    private final IEmailTokensRepository emailTokensRepository;

    public TokenService(ITokensRepository repository,
                        IUserRepository userRepository,
                        IEmailTokensRepository emailTokensRepository){
        this.repository = repository;
        this.userRepository = userRepository;
        this.emailTokensRepository = emailTokensRepository;
    }

    @Transactional
    public String create(String username){
        String token;
        long start = System.currentTimeMillis();
        Long userId = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));        

        if(this.repository.existsAnActiveToken(userId)){
            Tokens currentToken = this.repository.getCurrentTokenByUser(userId);
            currentToken.setRevoked(true);
            this.repository.saveAndFlush(currentToken);
        }

        token = UUID.randomUUID().toString();
        User user = this.userRepository.getReferenceById(userId);

        Tokens newToken = new Tokens();
        newToken.setTokenHash(token);
        newToken.setAssignedTo(user);
        newToken.setExpireAt(OffsetDateTime.now().plusDays(7));
        newToken.setTokenType((short) 3);
        newToken.setRevoked(false);

        this.repository.save(newToken);
        System.out.println("REVOKE AND INSERT NEW TOKEN: " + (System.currentTimeMillis() - start));
        return token;
    }

    @Transactional(readOnly = true)
    public String validateRefreshToken(String tokenHash){
        Tokens token = this.repository.findByTokenHashAndRevoked(tokenHash, false)
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
        
        EmailTokens newToken = new EmailTokens();
        newToken.setTokenHash(token);
        newToken.setAssignedTo(user);
        newToken.setExpireAt(OffsetDateTime.now().plusMinutes(15));
        newToken.setTokenType((short) 1);
        newToken.setUsed(false);

        this.emailTokensRepository.save(newToken);
        return token;
    }

    public void validateEmailToken(String tokenHash){
        EmailTokens token = this.emailTokensRepository.findToken(tokenHash);
        if(token == null || token.isUsed() || token.getExpireAt().isBefore(OffsetDateTime.now()))
            throw new InvalidTokenException("Invalid or expired token");

        User user = token.getAssignedTo();
        if (user == null)
            throw new InvalidTokenException("Invalid token");

        user.setStatus("active");
        this.userRepository.save(user);

        token.setUsed(true);
        this.emailTokensRepository.save(token);
    }
}

