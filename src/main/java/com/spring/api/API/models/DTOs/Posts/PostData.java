package com.spring.api.API.models.DTOs.Posts;


import java.time.OffsetDateTime;

public record PostData(
        Long id,
        String description,
        String picture,
        String username,
        Long likes,
        Long comments,
        OffsetDateTime datecreated
) {}
