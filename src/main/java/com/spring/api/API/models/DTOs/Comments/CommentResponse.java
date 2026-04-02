package com.spring.api.API.models.DTOs.Comments;

import java.time.OffsetDateTime;

public record CommentResponse(
        Long id,
        String content,
        OffsetDateTime datecreated,
        String username,
        Long parentId,
        Long postId
) {}
