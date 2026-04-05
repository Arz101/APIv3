package com.spring.api.API.models.BlockedUsers;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public record BlockedUsersId(
        Long userId,
        Long blockedId
) implements Serializable {}
