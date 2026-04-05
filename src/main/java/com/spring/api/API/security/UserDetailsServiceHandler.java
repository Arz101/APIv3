package com.spring.api.API.security;

import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.DTOs.Auth.UserDetailCredentials;
import com.spring.api.API.security.Exceptions.AccountException;
import com.spring.api.API.services.RankingService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
public class UserDetailsServiceHandler implements UserDetailsService {

    private final IUserRepository repository;
    private final RankingService service;

    public UserDetailsServiceHandler(IUserRepository repository,
                                     RankingService service){
        this.repository = repository;
        this.service = service;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetailCredentials user = this.repository.getCredentials(username)
                .orElseThrow(() -> new UsernameNotFoundException("INVALID CREDENTIALS"));

        if (!user.status().equals("active"))
            throw new AccountException("Account is not active, check your email for confirmation");

        return new org.springframework.security.core.userdetails.User(
                user.username(),
                user.password(),
                new ArrayList<>()
        );
    }
}
