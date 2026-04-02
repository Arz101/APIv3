package com.spring.api.API.models.DTOs.Comments;

import java.time.OffsetDateTime;

public record ReplyResponse (
        Long id,
        String username,
        String content,
        OffsetDateTime dateCreated,
        Long parentId
) {}
