package com.spring.api.API.services;

import com.spring.api.API.Repositories.IFollowsRepository;
import com.spring.api.API.Repositories.IProfileRepository;
import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.Follows.Follows;
import com.spring.api.API.models.User;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class FollowService {

    private final IFollowsRepository repository;
    private final IUserRepository userRepository;
    private final IProfileRepository profileRepository;
    private final FollowsGraph graph;
    private static final Logger log = LoggerFactory.getLogger(FollowService.class);

    public FollowService(
            IFollowsRepository repository,
            IUserRepository userRepository,
            IProfileRepository profileRepository,
            FollowsGraph graph
    ){
        this.repository = repository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.graph = graph;
    }

    @Transactional
    public Map<String, String> follow_user(String target, String currentUser){
        User userTarget = this.userRepository.findByUsername(target)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long user_id = this.userRepository.getIdByUsername(currentUser)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        User user = this.userRepository.getReferenceById(user_id);

        String status = "active";
        String request = "Now following user";

        if (this.profileRepository.isPrivate(userTarget.getId())){
            status = "pending";
            request = "Follow request sent";
        }

        this.repository.save(new Follows(
                        user,
                        userTarget,
                        status
                )
        );
        return Map.of("message", request);
    }

    public List<String> getFollowerUsernames(String username){
        return this.repository.findFollowersUsernames(username);
    }

    public List<String> getFollowingsUsernames(String username){
        return this.repository.findFollowedUsernames(username);
    }

    @Transactional(readOnly = true)
    public List<?> suggestionFollows(@NonNull UserDetails user){
        var currentUserId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        return graph.suggestionsByGraph(user, currentUserId);
    }

    @Transactional
    public void unfollow(Long userId, String username){
        var currentUser = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));

        var follow = this.repository.findByFollowerIdAndFollowedId(currentUser, userId)
                .orElseThrow(() -> new RuntimeException("Follow not exists"));
        this.repository.delete(follow);
    }

    @Transactional
    public Map<String, String> acceptRequest(Long followerId, @NonNull UserDetails user){
        Long userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));

        var follow = this.repository.getFollowRequest(userId, followerId)
                .orElseThrow(() -> new RuntimeException("Follow request not exists"));

        follow.setStatus("active");
        this.repository.save(follow);
        String message = "User: 994 now is following user: %s".formatted(user.getUsername());
        return Map.of("message", message);
    }

    @Transactional
    public Map declineFollowRequest(Long followerId, @NonNull UserDetails user) {
        Long userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));

        var follow = this.repository.getFollowRequest(userId, followerId)
                .orElseThrow(() -> new RuntimeException("Follow request not exists"));

        this.repository.delete(follow);
        return Map.of("message","Declined successfully!");
    }

    public Set<?> mutualFollows(@NonNull UserDetails user){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("not found"));

        return this.graph.mutualFollows(user, userId);
    }
}
