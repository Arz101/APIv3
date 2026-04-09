package com.spring.api.API.security;

import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.DTOs.Auth.UserDetailCredentials;
import com.spring.api.API.security.Exceptions.AccountException;
import com.spring.api.API.services.CacheAsyncHelper;
import com.spring.api.API.services.FeedService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
public class UserDetailsServiceHandler implements UserDetailsService {

    private final IUserRepository repository;
    private final CacheAsyncHelper cacheAsyncHelper;

    public UserDetailsServiceHandler(IUserRepository repository,
                                     FeedService service,
                                     CacheAsyncHelper cacheAsyncHelper) {
        this.repository = repository;
        this.cacheAsyncHelper = cacheAsyncHelper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetailCredentials user = this.repository.getCredentials(username)
                .orElseThrow(() -> new UsernameNotFoundException("INVALID CREDENTIALS"));

        if (!user.status().equals("active"))
            throw new AccountException("Account is not active, check your email for confirmation");

        this.cacheAsyncHelper.createFeedAsync(user.username(), user.userId());

        return new org.springframework.security.core.userdetails.User(
                user.username(),
                user.password(),
                new ArrayList<>()
        );
    }
}
