package com.spring.api.API.models.DTOs.User;

import java.time.OffsetDateTime;

public record UsersBlocked(
        Long blockedUserId,
        String username,
        OffsetDateTime datecreated
) {}
