package com.spring.api.API.follows;

import com.spring.api.API.profiles.IProfileRepository;
import com.spring.api.API.services.SocialDataStore;
import com.spring.api.API.users.IUserRepository;
import com.spring.api.API.follows.Follows.Follows;
import com.spring.api.API.users.User;
import com.spring.api.API.security.Exceptions.FollowNotFoundException;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import com.spring.api.API.users.dtos.UserFound;
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
    private final SocialRecommendationService graph;
    private final SocialDataStore store;
    private static final Logger log = LoggerFactory.getLogger(FollowService.class);

    public FollowService(
            IFollowsRepository repository,
            IUserRepository userRepository,
            IProfileRepository profileRepository,
            SocialRecommendationService graph,
            SocialDataStore store
    ){
        this.repository = repository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.graph = graph;
        this.store = store;
    }

    @Transactional
    public Map<String, String> followUser(String target, String currentUser){
        User userTarget = this.userRepository.findByUsername(target)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long userId = this.userRepository.getIdByUsername(currentUser)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        var user = this.userRepository.getReferenceById(userId);

        String status = "active";
        String request = "Now following user";

        if (this.profileRepository.isPrivate(userTarget.getId())){
            status = "pending";
            request = "Follow request sent";
        }

        this.repository.save(new Follows(user,userTarget,status));
        this.store.newFollow(currentUser, userId, target, userTarget.getId());
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
        return graph.usersWithSameInterests(user, currentUserId);
    }

    @Transactional(readOnly = true)
    public Map amIFollowing(UserDetails user, String following){
        var currentUserId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        var followingId = this.userRepository.getIdByUsername(following)
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        boolean exists = this.repository.isFollowOf(currentUserId, followingId);
        return Map.of("message", exists);
    }

    @Transactional
    public void unfollow(String target, String username){
        var currentUser = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));

        var targetId = this.userRepository.getIdByUsername(target)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));
        var follow = this.repository.findByFollowerIdAndFollowedId(currentUser, targetId)
                .orElseThrow(() -> new FollowNotFoundException("Follow not exists"));
        this.repository.delete(follow);
    }

    @Transactional
    public Map<String, String> acceptRequest(Long followerId, @NonNull UserDetails user){
        Long userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));

        var follow = this.repository.getFollowRequest(userId, followerId)
                .orElseThrow(() -> new FollowNotFoundException("Follow request not exists"));

        follow.setStatus("active");
        this.repository.save(follow);
        String message = "Now is following user: %s".formatted(user.getUsername());
        return Map.of("message", message);
    }

    @Transactional
    public Map declineFollowRequest(Long followerId, @NonNull UserDetails user) {
        Long userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));

        var follow = this.repository.getFollowRequest(userId, followerId)
                .orElseThrow(() -> new FollowNotFoundException("Follow request not exists"));

        this.repository.delete(follow);
        return Map.of("message","Declined successfully!");
    }

    public Set<?> mutualFollows(@NonNull UserDetails user){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("not found"));

        return this.graph.mutualFollows(user, userId);
    }

    public List<UserFound> followsRequests(UserDetails user){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));
        return this.repository.findAllFollowRequestByUserId(userId);
    }
}
