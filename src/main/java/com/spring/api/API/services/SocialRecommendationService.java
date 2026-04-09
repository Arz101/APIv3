package com.spring.api.API.services;

import com.spring.api.API.models.DTOs.User.UserNode;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SocialRecommendationService {

    private final SocialDataStore service;
    private static final Logger log = LoggerFactory.getLogger(SocialRecommendationService.class);

    public SocialRecommendationService(SocialDataStore service){
        this.service = service;
    }

    public Set<UserNode> mutualFollows(@NonNull UserDetails user, Long userId) {
        var currentUser = new UserNode(userId, user.getUsername());
        var following = this.service.getFollowsGraph().getOrDefault(currentUser, Set.of());

        log.info("following size {}" , following.size());
        return following.stream()
                .filter(friend -> this.service.getFollowsGraph().getOrDefault(friend, Set.of())
                        .contains(currentUser)
                )
                .collect(Collectors.toSet());
    }

    public List<?> suggestionsByGraph(UserDetails user, Long userId){
        var mutualFollows = this.mutualFollows(user, userId);
        Map<UserNode, Integer> relevanceCount = new HashMap<>();

        var currentUser = new UserNode(userId, user.getUsername());
        var myFollowings = this.service.getFollowsGraph().getOrDefault(currentUser, Set.of());

        for (var follow : mutualFollows){
            var following = this.service.getFollowsGraph().getOrDefault(follow, Set.of());
            for (var f : following){
                if(f.equals(currentUser) || myFollowings.contains(f)) continue;

                if (!relevanceCount.containsKey(f)){
                    relevanceCount.put(f, 1);
                } else relevanceCount.put(f, relevanceCount.get(f) + 1);
            }
        }
        return relevanceCount.entrySet().stream()
                .sorted(Map.Entry.<UserNode, Integer>comparingByValue().reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .toList();
    }
}
