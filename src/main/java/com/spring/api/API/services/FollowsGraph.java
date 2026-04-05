package com.spring.api.API.services;

import com.spring.api.API.Repositories.IFollowsRepository;
import com.spring.api.API.models.DTOs.Follows.FollowResponse;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
class UserNode {
    public Long userId;
    public String username;

    public UserNode(Long userId, String username){
        this.userId = userId;
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserNode userNode = (UserNode) o;
        return Objects.equals(userId, userNode.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}

@Component
@EnableAsync
public class FollowsGraph {
    private Map<UserNode, Set<UserNode>> graph = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(FollowsGraph.class);

    private final IFollowsRepository repository;
    public FollowsGraph(IFollowsRepository followsRepository){
        this.repository = followsRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init(){
        this.build();
    }

    @Async
    @Transactional(readOnly = true)
    private void build(){
        var follows = this.repository.getAll();
        for (var f : follows){
            this.graph.computeIfAbsent(new UserNode(f.followerId(), f.followerUsername()), k -> new HashSet<>())
                    .add(new UserNode(f.followedId(), f.followedUsername()));
        }

        log.info("Graph charged!");
        log.info("Graph Size: {}",graph.size());
    }

    public Set<UserNode> mutualFollows(@NonNull UserDetails user, Long userId) {
        var currentUser = new UserNode(userId, user.getUsername());
        var following = graph.getOrDefault(currentUser, Set.of());

        log.info("following size {}" , following.size());
        return following.stream()
                .filter(friend -> this.graph.getOrDefault(friend, Set.of())
                        .contains(currentUser)
                )
                .collect(Collectors.toSet());
    }

    public List<?> suggestionsByGraph(UserDetails user, Long userId){
        var mutualFollows = this.mutualFollows(user, userId);
        Map<UserNode, Integer> relevanceCount = new HashMap<>();

        var currentUser = new UserNode(userId, user.getUsername());
        var myFollowings = this.graph.getOrDefault(currentUser, Set.of());

        for (var follow : mutualFollows){
            var following = this.graph.getOrDefault(follow, Set.of());
            for (var f : following){
                if(f.equals(currentUser) || myFollowings.contains(f)) continue;

                if (!relevanceCount.containsKey(f)){
                    relevanceCount.put(f, 1);
                } else relevanceCount.put(f, relevanceCount.get(f) + 1);
            }
        }
        PriorityQueue<Map.Entry<UserNode, Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<UserNode, Integer> entry : relevanceCount.entrySet()) {
            pq.offer(entry);
            if (pq.size() > 50) {
                pq.poll();
            }
        }

        return pq.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<FollowResponse> suggestionByCommonFollows(Long userId){
        var suggestions = this.repository.getFollowsSuggestionByMutualFollows(userId);

        return suggestions.stream()
                .map(s -> new FollowResponse(s.getUsername(), s.getId()))
                .collect(Collectors.toList());
    }
}
