package com.spring.api.API.services;

import com.spring.api.API.Repositories.IBlockedUsersRepository;
import com.spring.api.API.Repositories.IEmailTokensRepository;
import com.spring.api.API.Repositories.IFollowsRepository;
import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.DTOs.User.CreateUserDTO;
import com.spring.api.API.models.User;
import com.spring.api.API.security.Exceptions.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock private IUserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenService tokenService;
    @Mock private IBlockedUsersRepository blockedUsersRepository;
    @Mock private IFollowsRepository followsRepository;
    @Mock private IEmailTokensRepository emailTokensRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createNewUserAndUsernameAndEmailNoExists() {
        var newUser = new CreateUserDTO("arz","mail", "12345", "active");
        User currentUser = new User("arz", "test", "12345", "active");
        currentUser.setId(1L);
        when(userRepository.existsByUsername("arz")).thenReturn(false);
        when(userRepository.existsByEmail("mail")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(currentUser);
        var result = userService.create(newUser);

        verify(userRepository, times(1)).saveAndFlush(any(User.class));
        assertThat(result).isNotNull();
    }

    @Test
    void createNewUserAndUsernameExists_ThrowException() {
        var newUser = new CreateUserDTO("arz","mail", "12345", "active");
        User currentUser = new User("arz", "test", "12345", "active");
        currentUser.setId(1L);
        when(userRepository.existsByUsername("arz")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.create(newUser));
    }

    @Test
    void createNewUserAndEmailExists_ThrowException() {
        var newUser = new CreateUserDTO("arz","mail", "12345", "active");
        User currentUser = new User("arz", "test", "12345", "active");
        currentUser.setId(1L);
        when(userRepository.existsByUsername("arz")).thenReturn(false);
        when(userRepository.existsByEmail("mail")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.create(newUser));
    }
}
