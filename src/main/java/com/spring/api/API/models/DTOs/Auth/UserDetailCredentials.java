package com.spring.api.API.models.DTOs.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record UserDetailCredentials (
        Long userId,
        String username,
        String password,
        String status
) {}
