package com.spring.api.API.models.DTOs.User;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserDTO (
    @NotBlank String username,
    String password,
    String newPassword
) {}
