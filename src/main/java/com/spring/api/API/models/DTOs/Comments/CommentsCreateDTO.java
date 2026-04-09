package com.spring.api.API.models.DTOs.Comments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentsCreateDTO (
    @NotNull Long postId,
    @NotBlank String content
) {}
