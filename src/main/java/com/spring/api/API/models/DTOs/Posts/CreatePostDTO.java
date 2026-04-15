package com.spring.api.API.models.DTOs.Posts;

import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

public record CreatePostDTO (
    @NotBlank String description,
    String picture,
    Set<String> hashtags
) {
    public CreatePostDTO{
        if(hashtags == null) hashtags = new HashSet<>();
    }
}
