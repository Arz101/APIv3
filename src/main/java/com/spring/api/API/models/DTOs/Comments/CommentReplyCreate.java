package com.spring.api.API.models.DTOs.Comments;

import jakarta.validation.constraints.NotBlank;

public record CommentReplyCreate(
        @NotBlank String content
) {
}
