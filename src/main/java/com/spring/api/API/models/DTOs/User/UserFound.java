package com.spring.api.API.models.DTOs.User;

public record UserFound(
        Long id,
        String username,
        String avatar_url
) {
}
