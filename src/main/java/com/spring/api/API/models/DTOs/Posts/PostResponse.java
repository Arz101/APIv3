package com.spring.api.API.models.DTOs.Posts;

import java.util.Set;

public record PostResponse(
    PostData post,
    Set<String> hashtags
) {}
