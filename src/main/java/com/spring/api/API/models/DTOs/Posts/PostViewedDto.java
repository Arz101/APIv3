package com.spring.api.API.models.DTOs.Posts;

public record PostViewedDto(
        String username,
        Long userId,
        Long postId
) {}
