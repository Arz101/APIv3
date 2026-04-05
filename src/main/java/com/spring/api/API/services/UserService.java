package com.spring.api.API.services;

import com.spring.api.API.Repositories.IBlockedUsersRepository;
import com.spring.api.API.Repositories.IFollowsRepository;
import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.BlockedUsers.BlockedUsers;
import com.spring.api.API.models.DTOs.User.*;
import com.spring.api.API.models.Follows.Follows;
import com.spring.api.API.models.Follows.FollowsId;
import com.spring.api.API.models.User;
import com.spring.api.API.security.Exceptions.EmailException;
import com.spring.api.API.security.Exceptions.UserAlreadyExistsException;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final IUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final IBlockedUsersRepository blockedUsersRepository;
    private final IFollowsRepository followsRepository;
    private final FollowsGraph graph;

    public UserService(IUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       TokenService tokenService,
                       IBlockedUsersRepository blockedUsersRepository,
                       IFollowsRepository followsRepository,
                       FollowsGraph graph){
        this.repository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.blockedUsersRepository = blockedUsersRepository;
        this.followsRepository = followsRepository;
        this.graph = graph;
    }

    @Transactional
    public UserResponseDTO create(@NonNull CreateUserDTO user){
        if (this.repository.existsByUsername(user.username().toLowerCase()) ||
                this.repository.existsByEmail(user.email()))
            throw new UserAlreadyExistsException("User already exists");

        String encodedPassword = this.passwordEncoder.encode(user.password());

        User new_user = repository.saveAndFlush(new User(
                user.username().toLowerCase(),
                user.email(),
                encodedPassword,
                user.status()
        ));

        String token = this.tokenService.saveAccountTokens(new_user);

        try {
            //this.emailService.sendHTMLEmail(new_user.getEmail(), "Confirm your account", token);
        } catch (Exception e) {
            throw new EmailException("Failed to send confirmation email: " + e.getMessage());
        }

        return new UserResponseDTO(
                new_user.getId(),
                new_user.getUsername(),
                new_user.getEmail(),
                new_user.getStatus()
        );
    }

    public UserResponseDTO findById(Long id){
        User user = this.repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findByUsername(String username){
        User user = this.repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus()
        );
    }

    @Transactional
    public UserResponseDTO updateUser(@NonNull UpdateUserDTO new_data, String username){
        User curr = this.repository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Something went wrong"));
        
        if (this.repository.existsByUsername(new_data.username())){
            throw new UserAlreadyExistsException("Username already in use");
        }

        curr.setUsername(new_data.username());
        if (this.passwordEncoder.matches(new_data.password(), curr.getPassword())){
            curr.setPassword(this.passwordEncoder.encode((new_data.new_password())));
        }
        this.repository.save(curr);
        return new UserResponseDTO(
            curr.getId(),
            curr.getUsername(),
            curr.getEmail(),
            curr.getStatus()
        );
    }

    @Transactional
    public Map blockUser(Long blockId, @NonNull UserDetails user){
        var userId = this.repository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));

        var followA = this.followsRepository.getFollowByFollowingAndFollowerId(blockId, userId);
        var followB = this.followsRepository.getFollowByFollowingAndFollowerId(userId, blockId);

        followA.ifPresent(this.followsRepository::delete);
        followB.ifPresent(this.followsRepository::delete);

        //delete follow by blockedUser
        //delete likes posts by blockedUser
        //delete comments by blockedUser
        //delete postsSaved blockedUser
        // ...

        this.blockedUsersRepository.save(new BlockedUsers(userId, blockId));
        return Map.of("message", "User blocked successfully");
    }

    @Transactional
    public Map unblockUser(Long blockId, @NonNull UserDetails user){
        var userId = this.repository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));

        var blocked = this.blockedUsersRepository.getBlockedUserByBlockedIdAndUserId(blockId, userId);

        blocked.ifPresent(this.blockedUsersRepository::delete);
        return Map.of("message", "User unblocked successfully!");
    }

    @Transactional(readOnly = true)
    public List<UsersBlocked> usersBlockedList(@NonNull UserDetails user){
        var userId = this.repository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));

        return this.blockedUsersRepository.getBlockedUsersUsernames(userId);
    }

    @Transactional(readOnly = true)
    public List<UserFound> usersFoundByText(String text, UserDetails user){
        return this.repository.usersFoundByText(text.toLowerCase());
    }
}
