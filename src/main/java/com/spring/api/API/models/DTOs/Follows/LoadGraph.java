package com.spring.api.API.models.DTOs.Follows;

public record LoadGraph(
        Long followerId,
        Long followedId,
        String followedUsername,
        String followerUsername
) {
}
